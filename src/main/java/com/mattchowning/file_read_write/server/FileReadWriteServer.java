package com.mattchowning.file_read_write.server;

import com.mattchowning.file_read_write.BasicServer;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;

public class FileReadWriteServer extends BasicServer {

    private static final int MAX_BODY_LENGTH = 15000;
    private static final OAuthServerHandler O_AUTH_SERVER_HANDLER = new OAuthServerHandler();

    public FileReadWriteServer(int port) {
        super(port, new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline()
                  .addLast(new HttpServerCodec(),
                           new HttpObjectAggregator(MAX_BODY_LENGTH),
                           O_AUTH_SERVER_HANDLER,
                           new FileReadWriteServerHandler());
            }
        });
    }

    public static void main(String[] args) throws Exception {
        new FileReadWriteServer(readPort(args))
                .run();
    }
}
