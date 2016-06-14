package com.mattchowning.file_read_write.server;

import com.mattchowning.file_read_write.server.handler.*;
import com.mattchowning.file_read_write.server.model.OAuthTokenMap;

import io.netty.channel.*;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;

import static com.mattchowning.file_read_write.SharedConstants.*;

public class FileReadWriteServer {

    private static final int MAX_BODY_LENGTH = 15000;

    public static void main(String[] args) throws Exception {
        OAuthTokenMap tokenMap = new OAuthTokenMap();
        startOAuthServer(tokenMap);
        startFileServer(tokenMap);
    }

    private static void startOAuthServer(OAuthTokenMap tokenMap) {
        ChannelInitializer oAuthChannelInitializer = new ChannelInitializer() {
            @Override
            protected void initChannel(Channel ch) throws Exception {
                ch.pipeline().addLast(new HttpServerCodec(),
                                      new HttpObjectAggregator(MAX_BODY_LENGTH),
                                      new OAuthRequestHandler(tokenMap));
            }
        };
        startServer(OAUTH_HOST, OAUTH_PORT, oAuthChannelInitializer);
    }

    private static void startFileServer(OAuthTokenMap tokenMap) {
        ChannelInitializer fileChannelInitializer = new ChannelInitializer() {
            @Override
            protected void initChannel(Channel ch) throws Exception {
                ch.pipeline().addLast(new HttpServerCodec(),
                                      new HttpObjectAggregator(MAX_BODY_LENGTH),
                                      new OAuthVerificationHandler(tokenMap),
                                      new FileReadWriteHandler());
            }
        };
        startServer(FILE_HOST, FILE_PORT, fileChannelInitializer);
    }

    private static void startServer(String host,
                                    int port,
                                    ChannelHandler channelHandler) {
        BasicServer server = new BasicServer(host, port, channelHandler);
        new Thread(server).start();
    }
}
