package com.mattchowning.file_read_write.client;

import com.google.gson.JsonSyntaxException;
import com.mattchowning.file_read_write.server.model.OAuthModel;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.util.ReferenceCountUtil;

import static com.mattchowning.file_read_write.SharedConstants.GSON;
import static com.mattchowning.file_read_write.SharedConstants.RESPONSE_CHARSET;

public class InitialAuthHandler extends SimpleChannelInboundHandler<FullHttpResponse> {

    public interface Listener {
        void onOAuthReceived(ChannelHandlerContext ctx, OAuthModel oAuthModel);
    }

    private final Listener listener;

    public InitialAuthHandler(Listener listener) {
        this.listener = listener;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpResponse response) throws Exception {
        String responseBody = response.content().toString(RESPONSE_CHARSET);
        switch (response.status().code()) {
            case 200:
                OAuthModel oAuthModel = parseOAuthResponse(responseBody);
                if (oAuthModel != null) {
                    System.out.println("OAuth response received");
                    listener.onOAuthReceived(ctx, oAuthModel);
                } else {
                    ReferenceCountUtil.retain(response);
                    ctx.fireChannelRead(response);
                }
                return;
            default:
                System.out.println("OAuth ERROR: " + responseBody);
        }
    }

    private OAuthModel parseOAuthResponse(String responseBody) {
        try {
            return GSON.fromJson(responseBody, OAuthModel.class);
        } catch (JsonSyntaxException e) {
            return null;
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
