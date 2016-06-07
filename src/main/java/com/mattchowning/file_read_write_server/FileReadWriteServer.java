package com.mattchowning.file_read_write_server;

import com.mattchowning.BasicServer;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;

public class FileReadWriteServer extends BasicServer {

    private static final int MAX_BODY_LENGTH = 15000;
    // TODO could just statically store a tokenStore Set here instead
    private static final FileReadWriteServerHandler FILE_READ_WRITE_SERVER_HANDLER = new FileReadWriteServerHandler();

    public FileReadWriteServer(int port) {
        super(port, new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline()
                  .addLast(new HttpServerCodec(),
                           new HttpObjectAggregator(MAX_BODY_LENGTH),
                           FILE_READ_WRITE_SERVER_HANDLER);
            }
        });
    }

    public static void main(String[] args) throws Exception {
        new FileReadWriteServer(readPort(args))
                .run();
    }
}
