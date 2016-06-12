package com.mattchowning.file_read_write.client.calls;

import com.mattchowning.file_read_write.client.InitialAuthHandler;
import com.mattchowning.file_read_write.server.model.OAuthToken;

import java.util.function.Supplier;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOutboundInvoker;
import io.netty.handler.codec.http.*;

import static com.mattchowning.file_read_write.SharedConstants.*;

public class GetOAuthCall extends Call<OAuthToken> {

    private final String username;
    private final String password;
    private final ChannelHandler[] handlers;
    private final Supplier<OAuthToken> resultSupplier;

    public GetOAuthCall(String username, String password) {
        this.username = username;
        this.password = password;

        InitialAuthHandler initialAuthHandler = new InitialAuthHandler();
        handlers = new ChannelHandler[] { new HttpClientCodec(),
                                          new HttpObjectAggregator(MAX_BODY_LENGTH),
                                          initialAuthHandler };
        resultSupplier = initialAuthHandler::getOAuthModel;
    }

    @Override
    protected ChannelHandler[] getChannelHandlers() {
        return handlers;
    }

    @Override
    protected Supplier<OAuthToken> getResultSupplier() {
        return resultSupplier;
    }

    @Override
    protected void makeRequest(ChannelOutboundInvoker ctx) {
        System.out.println("Requesting OAuth token...");
        FullHttpRequest message = getOAuthTokenRequest(username, password);
        ctx.writeAndFlush(message);
    }

    private FullHttpRequest getOAuthTokenRequest(String username, String password) {
        QueryStringEncoder queryStringEncoder = new QueryStringEncoder(OAUTH_PATH);
        queryStringEncoder.addParam(GRANT_TYPE_KEY, GRANT_TYPE_PASSWORD);
        queryStringEncoder.addParam(USERNAME_KEY, username);
        queryStringEncoder.addParam(PASSWORD_KEY, password);
        String uriString = queryStringEncoder.toString();
        return new DefaultFullHttpRequest(HttpVersion.HTTP_1_1,
                                          HttpMethod.POST,
                                          uriString);
    }
}
