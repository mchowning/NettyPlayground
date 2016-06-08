package com.mattchowning.file_read_write.client;

import java.util.Scanner;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;

public class FileReadWriteClient {

    private static final int MAX_BODY_LENGTH = 15000;

    private static final String GET_SELECTION = "g";
    private static final String POST_SELECTION = "p";
    private static final String EXIT_SELECTION = "e";

    private static final String HOST = "localhost";
    private static final int PORT = 8080;


    public static void main(String[] args) throws Exception {

        Scanner scanner = new Scanner(System.in);
        String question = String.format("Would you like to Get the file, Post changes to the file, or Exit [%s/%s/%s]?",
                                        GET_SELECTION,
                                        POST_SELECTION,
                                        EXIT_SELECTION);
        System.out.println(question);
        String action = scanner.nextLine();
        switch (action) {
            case GET_SELECTION:
                System.out.println("Get action selected.");
                getFileContents();
                break;
            case POST_SELECTION:
                System.out.println("Post action selected.");
                System.out.println("What file content would you like to post?");
                String newFileContent = scanner.nextLine();
                postFileContents(newFileContent);
                // FIXME get this working
                break;
            case EXIT_SELECTION:
                System.out.println("Exiting...");
                break;
            default:
                System.out.println("Invalid selection.");
        }
    }

    private static void getFileContents() {
        startServer(new HttpClientCodec(),
                    new HttpObjectAggregator(MAX_BODY_LENGTH),
                    new OAuthClientCombinedHandler(),
                    //new OAuthRenewalHandler(authModel)),
                    new GetFileClientHandler());
    }

    private static void postFileContents(String newFileContent) {
        startServer(new HttpClientCodec(),
                    new HttpObjectAggregator(MAX_BODY_LENGTH),
                    new OAuthClientCombinedHandler(),
                    //new OAuthRenewalHandler(authModel)),
                    new PostFileClientHandler(newFileContent));
    }

    private static void startServer(ChannelHandler... channelHandlers) {

        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {

            Bootstrap b = new Bootstrap();
            b.group(workerGroup)
             .channel(NioSocketChannel.class)
             .option(ChannelOption.SO_KEEPALIVE, true)
             .handler(new ChannelInitializer<SocketChannel>() {
                 @Override
                 protected void initChannel(SocketChannel ch) throws Exception {
                     ch.pipeline().addLast(channelHandlers);
                 }
             });

            ChannelFuture f = b.connect(HOST, PORT).sync();

            f.channel().closeFuture().sync();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            workerGroup.shutdownGracefully();
        }
    }
}
