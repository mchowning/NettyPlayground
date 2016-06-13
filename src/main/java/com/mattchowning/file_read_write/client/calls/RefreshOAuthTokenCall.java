package com.mattchowning.file_read_write.client.calls;

import io.netty.channel.ChannelOutboundInvoker;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.HttpPostRequestEncoder;

import static com.mattchowning.file_read_write.SharedConstants.*;

public class RefreshOAuthTokenCall extends OAuthCall {

    private final String refreshToken;

    public RefreshOAuthTokenCall(String refreshToken) {
        this.refreshToken = refreshToken;
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