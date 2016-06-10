package com.mattchowning.file_read_write.client;

import java.util.Scanner;
import java.util.function.Consumer;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelOutboundInvoker;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpMessage;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;

public class FileReadWriteClient {

    private static final int MAX_BODY_LENGTH = 15000;

    private static final String GET_SELECTION = "g";
    private static final String POST_SELECTION = "p";
    private static final String EXIT_SELECTION = "e";

    private static final String HOST = "localhost";
    private static final int PORT = 8080;

    private static final Scanner scanner = new Scanner(System.in);
    private static final OAuthClientCombinedHandler oAuthClientCombinedHandler = new OAuthClientCombinedHandler();

    public static void main(String[] args) throws Exception {
        getUserSelection();
    }

    private static void getUserSelection() {
        switch (askForUserSelection()) {
            case GET_SELECTION:
                System.out.println("Get action selected.");
                startServer(FileReadWriteClient::getFileContent);
                break;
            case POST_SELECTION:
                System.out.println("Post action selected.");
                System.out.println("What file content would you like to post?");
                String newFileContent = scanner.nextLine();
                startServer(getPostFileCall(newFileContent));
                break;
            case EXIT_SELECTION:
                System.out.println("Exiting...");
                break;
            default:
                System.out.println("Invalid selection.");
                getUserSelection();
        }
    }

    private static String askForUserSelection() {
        String question = String.format("Would you like to Get the file, Post changes to the file, or Exit [%s/%s/%s]?",
                                        GET_SELECTION,
                                        POST_SELECTION,
                                        EXIT_SELECTION);
        System.out.println();
        System.out.println(question);
        return scanner.nextLine();
    }

    private static void startServer(Consumer<ChannelOutboundInvoker> httpCall) {

        EventLoopGroup workerGroup = new NioEventLoopGroup();

        Bootstrap b = new Bootstrap();
        b.group(workerGroup)
         .channel(NioSocketChannel.class)
         .option(ChannelOption.SO_KEEPALIVE, true)
         .handler(new ChannelInitializer<SocketChannel>() {
             @Override
             protected void initChannel(SocketChannel ch) throws Exception {
                 ch.pipeline().addLast(new HttpClientCodec(),
                                       new HttpObjectAggregator(MAX_BODY_LENGTH),
                                       oAuthClientCombinedHandler,
                                       new ReadInboundFileClientHandler());
             }
         });

        try {

            ChannelFuture f = b.connect(HOST, PORT).sync();
            f.channel().closeFuture().addListener(future -> getUserSelection());

            httpCall.accept(f.channel());

            f.channel().closeFuture().sync();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            workerGroup.shutdownGracefully();
        }
    }

    private static Consumer<ChannelOutboundInvoker> getPostFileCall(String newFileContent) {
        return ctx -> {
            FullHttpMessage message = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1,
                                                                 HttpMethod.POST,
                                                                 "",
                                                                 Unpooled.copiedBuffer(newFileContent, CharsetUtil.UTF_8));
            message.headers().add(HttpHeaderNames.CONTENT_LENGTH, newFileContent.length());
            System.out.println("Posting updated file content...");
            ctx.writeAndFlush(message);
        };
    }

    private static void getFileContent(ChannelOutboundInvoker ctx) {
        System.out.println("Requesting file content...");
        FullHttpMessage message = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "");
        ctx.writeAndFlush(message);
    }
}
