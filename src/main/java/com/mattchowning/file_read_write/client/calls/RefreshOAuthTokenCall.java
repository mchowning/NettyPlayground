package com.mattchowning.file_read_write.client.calls;

import com.mattchowning.file_read_write.client.InitialAuthHandler;
import com.mattchowning.file_read_write.server.model.OAuthToken;

import java.util.function.Supplier;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOutboundInvoker;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.HttpPostRequestEncoder;

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
        try {
            FullHttpRequest request = getRefreshTokenRequest();
            ctx.writeAndFlush(request);
        } catch (HttpPostRequestEncoder.ErrorDataEncoderException e) {
            e.printStackTrace();
            ctx.close();
        }
    }

    private FullHttpRequest getRefreshTokenRequest() throws HttpPostRequestEncoder.ErrorDataEncoderException {
        FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1,
                                                             HttpMethod.POST,
                                                             OAUTH_PATH);
        HttpPostRequestEncoder postRequestEncoder = new HttpPostRequestEncoder(request, false);
        postRequestEncoder.addBodyAttribute(GRANT_TYPE_KEY, GRANT_TYPE_REFRESH_TOKEN);
        postRequestEncoder.addBodyAttribute(REFRESH_TOKEN_KEY, refreshToken);
        postRequestEncoder.finalizeRequest();
        return request;
    }
}
