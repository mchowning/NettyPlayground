package com.mattchowning.discard_server;

import com.mattchowning.BasicServer;

public class DiscardServer extends BasicServer {

    public DiscardServer(int port) {
        super(port, new DiscardServerHandler());
    }

    public static void main(String[] args) throws Exception {
        new DiscardServer(readPort(args))
                .run();
    }
}
