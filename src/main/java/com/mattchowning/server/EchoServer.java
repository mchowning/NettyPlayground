package com.mattchowning.server;

import com.mattchowning.server.handler.EchoServerHandler;

public class EchoServer extends BasicServer {
    public EchoServer(int port) {
        super(port, new EchoServerHandler());
    }

    public static void main(String[] args) throws Exception {
        new EchoServer(readPort(args))
                .run();
    }
}
