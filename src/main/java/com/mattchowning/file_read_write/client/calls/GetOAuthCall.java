package com.mattchowning.file_read_write.client.calls;

import io.netty.channel.ChannelOutboundInvoker;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.HttpPostRequestEncoder;

import static com.mattchowning.file_read_write.SharedConstants.*;

public class GetOAuthCall extends OAuthCall {

    private final String username;
    private final String password;

    public GetOAuthCall(String username, String password) {
        super();
        this.username = username;
        this.password = password;
    }

    @Override
    protected void makeRequest(ChannelOutboundInvoker ctx) {
        System.out.println("Requesting OAuth token...");
        try {
            FullHttpRequest request = getOAuthTokenRequest();
            ctx.writeAndFlush(request);
        } catch (HttpPostRequestEncoder.ErrorDataEncoderException e) {
            e.printStackTrace();
            ctx.close();
        }
    }

    private FullHttpRequest getOAuthTokenRequest() throws HttpPostRequestEncoder.ErrorDataEncoderException {
        FullHttpRequest request =  new DefaultFullHttpRequest(HttpVersion.HTTP_1_1,
                                                              HttpMethod.POST,
                                                              OAUTH_PATH);

        HttpPostRequestEncoder postRequestEncoder = new HttpPostRequestEncoder(request, false);
        postRequestEncoder.addBodyAttribute(GRANT_TYPE_KEY, GRANT_TYPE_PASSWORD);
        postRequestEncoder.addBodyAttribute(USERNAME_KEY, username);
        postRequestEncoder.addBodyAttribute(PASSWORD_KEY, password);
        postRequestEncoder.finalizeRequest();
        return request;
    }
}
