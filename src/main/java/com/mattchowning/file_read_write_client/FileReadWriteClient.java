package com.mattchowning.file_read_write_client;

import com.mattchowning.file_read_write_client.model.UserAction;
import com.mattchowning.file_read_write_server.model.OAuthModel;

import java.util.Scanner;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.util.AttributeKey;

public class FileReadWriteClient {

    public static final AttributeKey<UserAction> USER_SELECTION = AttributeKey.valueOf("user_selection");

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
                System.out.println("get action fired");
                authorizeUser(oAuthModel -> {
                    String msg = String.format("Token received: %s %s", oAuthModel.token_type, oAuthModel.access_token);
                    System.out.println(msg);
                    getFileContents(oAuthModel);
                });
                break;
            case POST_SELECTION:
                System.out.println("What file content would you like to post?");
                String newFileContent = scanner.nextLine();
                authorizeUser(oAuthModel -> {
                    String msg = String.format("Token received: %s %s", oAuthModel.token_type, oAuthModel.access_token);
                    System.out.println(msg);
                    postFileContents(oAuthModel, newFileContent);

                });
                System.out.println("post action fired");
                break;
            case EXIT_SELECTION:
                System.out.println("Exiting...");
                break;
            default:
                System.out.println("invalid selection");
        }
    }

    private static void authorizeUser(final InitialAuthHandler.InitialAuthListener initialAuthListener) throws InterruptedException {

        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {

            Bootstrap b = new Bootstrap();
            b.group(workerGroup)
             .channel(NioSocketChannel.class)
             .option(ChannelOption.SO_KEEPALIVE, true)
             .handler(new ChannelInitializer<SocketChannel>() {
                 @Override
                 protected void initChannel(SocketChannel ch) throws Exception {
                     ch.pipeline().addLast(new HttpClientCodec(),
                                           new HttpObjectAggregator(MAX_BODY_LENGTH),
                                           new InitialAuthHandler(initialAuthListener));
                 }
             });

            ChannelFuture f = b.connect(HOST, PORT).sync();

            f.channel().closeFuture().sync();

        } finally {
            workerGroup.shutdownGracefully();
        }
    }

    private static void getFileContents(OAuthModel authModel) {

        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {

            Bootstrap b = new Bootstrap();
            b.group(workerGroup)
             .channel(NioSocketChannel.class)
             .option(ChannelOption.SO_KEEPALIVE, true)
             .handler(new ChannelInitializer<SocketChannel>() {
                 @Override
                 protected void initChannel(SocketChannel ch) throws Exception {
                     ch.pipeline().addLast(new HttpClientCodec(), new HttpObjectAggregator(MAX_BODY_LENGTH),
                                           //new OAuthRenewalHandler(authModel),
                                           new GetFileClientHandler(authModel));
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

    private static void postFileContents(OAuthModel authModel, String newFileContent) {

        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {

            Bootstrap b = new Bootstrap();
            b.group(workerGroup)
             .channel(NioSocketChannel.class)
             .option(ChannelOption.SO_KEEPALIVE, true)
             .handler(new ChannelInitializer<SocketChannel>() {
                 @Override
                 protected void initChannel(SocketChannel ch) throws Exception {
                     ch.pipeline().addLast(new HttpClientCodec(), new HttpObjectAggregator(MAX_BODY_LENGTH),
                                           //new OAuthRenewalHandler(authModel),
                                           new PostFileClientHandler(authModel, newFileContent));
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
