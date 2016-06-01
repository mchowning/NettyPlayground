package com.mattchowning.time_server;

import com.mattchowning.BasicServer;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

public class TimeServer extends BasicServer {

    public TimeServer(int port) {
        super(port, new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline()
                  .addLast(new TimeEncoder(),
                           new TimeServerHandler());
            }
        });
    }

    public static void main(String[] args) throws Exception {
        new TimeServer(readPort(args))
                .run();
    }
}
