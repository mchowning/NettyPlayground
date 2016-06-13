package com.mattchowning.file_read_write.client.calls;

import com.mattchowning.file_read_write.client.InitialAuthHandler;
import com.mattchowning.file_read_write.server.model.OAuthToken;

import java.util.function.Supplier;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOutboundInvoker;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.HttpPostRequestEncoder;

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
