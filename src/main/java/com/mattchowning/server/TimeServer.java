package com.mattchowning.server;

import com.mattchowning.server.handler.TimeServerHandler;
import com.mattchowning.utils.TimeEncoder;

public class TimeServer extends BasicServer {

    public TimeServer(int port) {
        super(port, new TimeEncoder(), new TimeServerHandler());
    }

    public static void main(String[] args) throws Exception {
        new TimeServer(readPort(args))
                .run();
    }
}
