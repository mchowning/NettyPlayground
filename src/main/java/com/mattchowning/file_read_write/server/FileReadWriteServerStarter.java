package com.mattchowning.file_read_write.server;

import com.mattchowning.file_read_write.server.handler.*;
import com.mattchowning.file_read_write.server.model.OAuthTokenMap;

import static com.mattchowning.file_read_write.SharedConstants.*;

public class FileReadWriteServerStarter {

    public static void main(String[] args) throws Exception {
        OAuthTokenMap tokenMap = new OAuthTokenMap();
        startOAuthServer(tokenMap);
        startFileServer(tokenMap);
    }

    private static void startOAuthServer(OAuthTokenMap tokenMap) {
        HttpServer oAuthServer = new HttpServer(OAUTH_HOST, OAUTH_PORT,
                                                new OAuthRequestHandler(tokenMap));
        new Thread(oAuthServer).start();
    }

    private static void startFileServer(OAuthTokenMap tokenMap) {
        HttpServer fileServer = new HttpServer(FILE_HOST, FILE_PORT,
                                               new OAuthVerificationHandler(tokenMap),
                                               new FileReadWriteHandler());
        new Thread(fileServer).start();
    }
}
