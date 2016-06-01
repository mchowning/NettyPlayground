package com.mattchowning.echo_server;

import com.mattchowning.BasicServer;

public class EchoServer extends BasicServer {
    public EchoServer(int port) {
        super(port, new EchoServerHandler());
    }

    public static void main(String[] args) throws Exception {
        new EchoServer(readPort(args))
                .run();
    }
}
