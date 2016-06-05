package com.mattchowning.file_read_write_client;

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

public class FileReadWriteClient {

    private static final int MAX_BODY_LENGTH = 15000;

    public static void main(String[] args) throws Exception {

        //String host = args[0];
        //int port = Integer.parseInt(args[1]);
        //String username = args[2];
        //String password = args[3];

        String host = "localhost";
        int port = 8080;
        String username = "some_username";
        String password = "some_password";

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
                                           new OAuthClientHandler(username, password),
                                           new FileClientHandler());
                 }
             });

            ChannelFuture f = b.connect(host, port).sync();
            f.channel().closeFuture().sync();

        } finally {
            workerGroup.shutdownGracefully();
        }
    }
}
