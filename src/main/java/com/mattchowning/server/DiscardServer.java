package com.mattchowning.server;

import com.mattchowning.server.handler.DiscardServerHandler;

public class DiscardServer extends BasicServer {

    public DiscardServer(int port) {
        super(port, new DiscardServerHandler());
    }

    public static void main(String[] args) throws Exception {
        new DiscardServer(readPort(args))
                .run();
    }
}
