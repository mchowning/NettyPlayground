package com.mattchowning.file_read_write.client.handler;

import com.google.gson.JsonSyntaxException;
import com.mattchowning.file_read_write.server.model.OAuthToken;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;

import static com.mattchowning.file_read_write.SharedConstants.GSON;
import static com.mattchowning.file_read_write.SharedConstants.RESPONSE_CHARSET;

public class InitialAuthClientHandler extends SimpleChannelInboundHandler<FullHttpResponse> {

    private OAuthToken oAuthToken;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpResponse response) throws Exception {
        String responseBody = response.content().toString(RESPONSE_CHARSET);
        switch (response.status().code()) {
            case 200:
                OAuthToken oAuthToken = parseOAuthResponse(responseBody);
                if (oAuthToken != null) {
                    this.oAuthToken = oAuthToken;
                    System.out.println("OAuth response received.");
                    break;
                }
            default:
                System.out.println("OAuth ERROR: " + responseBody);
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
    }

    public OAuthToken getOAuthModel() {
        return oAuthToken;
    }
}
