package com.mattchowning.file_read_write.client.calls;

import com.mattchowning.file_read_write.client.InitialAuthHandler;
import com.mattchowning.file_read_write.server.model.OAuthToken;

import java.util.function.Supplier;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOutboundInvoker;
import io.netty.handler.codec.http.*;

import static com.mattchowning.file_read_write.SharedConstants.*;

public class RefreshOAuthTokenCall extends Call<OAuthToken> {

    private final String refreshToken;
    private final ChannelHandler[] handlers;
    private final Supplier<OAuthToken> resultSupplier;

    public RefreshOAuthTokenCall(String refreshToken) {
        this.refreshToken = refreshToken;
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
        System.out.println("Attempting to refresh OAuth token...");
        FullHttpRequest request = getRefreshTokenRequest();
        ctx.writeAndFlush(request);
    }

    /*
    FIXME all of these strings should be query encoded content, not urls

    For example, the client makes the following HTTP request using
    transport-layer security (with extra line breaks for display purposes
            only):

    POST /token HTTP/1.1
    Host: server.example.com
    Authorization: Basic czZCaGRSa3F0MzpnWDFmQmF0M2JW
    Content-Type: application/x-www-form-urlencoded

    grant_type=refresh_token&refresh_token=tGzv3JOkF0XG5Qx2TlKWIA
    */

    private FullHttpRequest getRefreshTokenRequest() {
        QueryStringEncoder queryStringEncoder = new QueryStringEncoder(OAUTH_PATH);
        queryStringEncoder.addParam(GRANT_TYPE_KEY, GRANT_TYPE_REFRESH_TOKEN);
        queryStringEncoder.addParam(REFRESH_TOKEN_KEY, refreshToken);
        String uriString = queryStringEncoder.toString();
        return new DefaultFullHttpRequest(HttpVersion.HTTP_1_1,
                                          HttpMethod.POST,
                                          uriString);
    }
}
