package com.mattchowning.time_server;

import com.mattchowning.BasicServer;

public class TimeServer extends BasicServer {

    public TimeServer(int port) {
        super(port, new TimeEncoder(), new TimeServerHandler());
    }

    public static void main(String[] args) throws Exception {
        new TimeServer(readPort(args))
                .run();
    }
}
