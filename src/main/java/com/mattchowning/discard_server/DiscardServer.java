package com.mattchowning.discard_server;

import com.mattchowning.BasicServer;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

public class DiscardServer extends BasicServer {

    public DiscardServer(int port) {
        super(port, new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(new DiscardServerHandler());
            }
        });
    }

    public static void main(String[] args) throws Exception {
        new DiscardServer(readPort(args))
                .run();
    }
}
