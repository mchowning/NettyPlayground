package com.mattchowning.file_read_write.client.handler;

import com.google.gson.JsonSyntaxException;
import com.mattchowning.file_read_write.server.model.OAuthToken;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;

import static com.mattchowning.file_read_write.SharedConstants.GSON;
import static com.mattchowning.file_read_write.SharedConstants.RESPONSE_CHARSET;

public class ClientInitialAuthHandler extends SimpleChannelInboundHandler<FullHttpResponse> {

    private final HandlerCallback<OAuthToken> callback;

    public ClientInitialAuthHandler(HandlerCallback<OAuthToken> callback) {
        this.callback = callback;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpResponse response) throws Exception {
        String responseBody = response.content().toString(RESPONSE_CHARSET);
        switch (response.status().code()) {
            case 200:
                OAuthToken oAuthToken = parseOAuthResponse(responseBody);
                if (oAuthToken != null) {
                    System.out.println("OAuth response received.");
                    callback.onSuccess(oAuthToken);
                    break;
                }
            default:
                System.out.println("OAuth ERROR: " + responseBody);
                callback.onError();
        }
        ctx.close();
    }

    private OAuthToken parseOAuthResponse(String responseBody) {
        try {
            return GSON.fromJson(responseBody, OAuthToken.class);
        } catch (JsonSyntaxException e) {
            return null;
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
        callback.onError();
    }
}
