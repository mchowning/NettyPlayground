package com.mattchowning.file_read_write_server;

import com.mattchowning.BasicServer;

import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;

public class FileReadWriteServer extends BasicServer {

    private static final int MAX_BODY_LENGTH = 15000;

    public FileReadWriteServer(int port) {
        super(port,
              new HttpServerCodec(),
              new HttpObjectAggregator(MAX_BODY_LENGTH),
              new FileReadWriteHandler());
    }

    public static void main(String[] args) throws Exception {
        new FileReadWriteServer(readPort(args))
                .run();
    }
}
