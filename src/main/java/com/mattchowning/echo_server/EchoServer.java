package com.mattchowning.echo_server;

import com.mattchowning.BasicServer;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

public class EchoServer extends BasicServer {
    public EchoServer(int port) {
        super(port, new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(new EchoServerHandler());
            }
        });
    }

    public static void main(String[] args) throws Exception {
        new EchoServer(readPort(args))
                .run();
    }
}
