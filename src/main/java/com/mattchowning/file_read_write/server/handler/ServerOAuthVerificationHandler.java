package com.mattchowning.file_read_write.server.handler;

import com.mattchowning.file_read_write.server.ServerUtil;
import com.mattchowning.file_read_write.server.model.OAuthToken;
import com.mattchowning.file_read_write.server.model.OAuthTokenMap;

import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;

@ChannelHandler.Sharable
public class ServerOAuthVerificationHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    public static final AttributeKey<Boolean> AUTHORIZED = AttributeKey.valueOf("isauthorized");

    private final OAuthTokenMap oAuthTokens;
    private final ServerUtil serverUtil;

    public ServerOAuthVerificationHandler(OAuthTokenMap oAuthTokenMap) {
        this(oAuthTokenMap, new ServerUtil());
    }

    protected ServerOAuthVerificationHandler(OAuthTokenMap oAuthTokenMap, ServerUtil serverUtil) {
        super();
        this.oAuthTokens = oAuthTokenMap;
        this.serverUtil = serverUtil;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        if (request.headers().contains(HttpHeaderNames.AUTHORIZATION)) {
            String encodedAuthHeader = request.headers().get(HttpHeaderNames.AUTHORIZATION);
            OAuthToken receivedOAuthToken = serverUtil.getOAuthToken(encodedAuthHeader);
            if (!receivedOAuthToken.hasValidTokenType()) {
                serverUtil.sendError(ctx, HttpResponseStatus.BAD_REQUEST, "invalid_grant", "Bearer token type required");
            } else if (!oAuthTokens.containsAccessToken(receivedOAuthToken)) {
                serverUtil.sendError(ctx, HttpResponseStatus.BAD_REQUEST, "invalid_grant", "Invalid token");
            } else if (oAuthTokens.getWithAccessToken(receivedOAuthToken.getAccessToken()).isExpired()) { // use saved token to check expiration
                serverUtil.sendError(ctx, HttpResponseStatus.BAD_REQUEST, "invalid_grant", "Token expired");
            } else {
                forwardRequest(ctx, request, true);
            }
        } else {
            forwardRequest(ctx, request, false);
        }
    }

    private void forwardRequest(ChannelHandlerContext ctx, FullHttpRequest request, boolean isAuthorized) {
        ctx.channel().attr(AUTHORIZED).set(isAuthorized);
        ReferenceCountUtil.retain(request);
        ctx.fireChannelRead(request);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
