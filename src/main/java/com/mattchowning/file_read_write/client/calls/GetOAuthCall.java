package com.mattchowning.file_read_write.client.calls;

import com.mattchowning.file_read_write.client.InitialAuthHandler;
import com.mattchowning.file_read_write.server.model.OAuthModel;

import java.util.function.Supplier;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOutboundInvoker;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpMessage;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.QueryStringEncoder;

import static com.mattchowning.file_read_write.SharedConstants.GRANT_TYPE_KEY;
import static com.mattchowning.file_read_write.SharedConstants.GRANT_TYPE_PASSWORD;
import static com.mattchowning.file_read_write.SharedConstants.OAUTH_PATH;
import static com.mattchowning.file_read_write.SharedConstants.PASSWORD_KEY;
import static com.mattchowning.file_read_write.SharedConstants.USERNAME_KEY;

public class GetOAuthCall extends Call<OAuthModel> {

    private final String username;
    private final String password;
    private final ChannelHandler[] handlers;
    private final Supplier<OAuthModel> resultSupplier;

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
    protected Supplier<OAuthModel> getResultSupplier() {
        return resultSupplier;
    }

    @Override
    protected void makeRequest(ChannelOutboundInvoker ctx) {
        FullHttpMessage message = getOAuthTokenRequest(username, password);
        System.out.println("Requesting OAuth token...");
        ctx.writeAndFlush(message);
    }

    private FullHttpMessage getOAuthTokenRequest(String username, String password) {
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
