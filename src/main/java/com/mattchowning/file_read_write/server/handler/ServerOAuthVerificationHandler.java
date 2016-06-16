package com.mattchowning.file_read_write.server.handler;

import com.mattchowning.file_read_write.server.ServerUtils;
import com.mattchowning.file_read_write.server.model.OAuthToken;
import com.mattchowning.file_read_write.server.model.OAuthTokenMap;

import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.util.ReferenceCountUtil;

@ChannelHandler.Sharable
public class ServerOAuthVerificationHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private final OAuthTokenMap oAuthTokens;
    private final ServerUtils serverUtils;

    public ServerOAuthVerificationHandler(OAuthTokenMap oAuthTokenMap) {
        this(oAuthTokenMap, new ServerUtils());
    }

    protected ServerOAuthVerificationHandler(OAuthTokenMap oAuthTokenMap, ServerUtils serverUtils) {
        super();
        this.oAuthTokens = oAuthTokenMap;
        this.serverUtils = serverUtils;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        if (hasValidToken(ctx, request)) {
            forwardRequest(ctx, request);
        }
    }

    private boolean hasValidToken(ChannelHandlerContext ctx, FullHttpRequest request) {
        boolean isAuthorized = false;
        if (request.headers().contains(HttpHeaderNames.AUTHORIZATION)) {
            String encodedAuthHeader = request.headers().get(HttpHeaderNames.AUTHORIZATION);
            OAuthToken receivedOAuthToken = OAuthToken.fromEncodedAuthorizationHeader(encodedAuthHeader);
            if (!receivedOAuthToken.hasValidTokenType()) {
                serverUtils.sendError(ctx, HttpResponseStatus.BAD_REQUEST, "invalid_grant", "Bearer token type required");
            } else if (!oAuthTokens.containsAccessToken(receivedOAuthToken.accessToken)) {
                serverUtils.sendError(ctx, HttpResponseStatus.BAD_REQUEST, "invalid_grant", "Invalid token");
            } else if (oAuthTokens.getWithAccessToken(receivedOAuthToken.accessToken).isExpired()) { // use saved token to check expiration
                serverUtils.sendError(ctx, HttpResponseStatus.BAD_REQUEST, "Token expired");
            } else {
                isAuthorized = true;
            }
        } else {
            serverUtils.sendError(ctx, HttpResponseStatus.BAD_REQUEST, "invalid_request", "authorization header required");
        }
        return isAuthorized;
    }

    private void forwardRequest(ChannelHandlerContext ctx, FullHttpRequest request) {
        ReferenceCountUtil.retain(request);
        ctx.fireChannelRead(request);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
