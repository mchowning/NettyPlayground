package com.mattchowning.file_read_write.server;

import com.mattchowning.file_read_write.server.model.OAuthToken;

import java.io.IOException;
import java.util.*;

import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.*;
import io.netty.util.ReferenceCountUtil;

import static com.mattchowning.file_read_write.SharedConstants.*;
import static com.mattchowning.file_read_write.server.ServerUtils.getDate;
import static com.mattchowning.file_read_write.server.ServerUtils.sendError;

@ChannelHandler.Sharable
public class OAuthServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private Map<String, OAuthToken> issuedTokens = new HashMap<>();
    private Map<String, OAuthToken> issuedRefreshTokens = new HashMap<>();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        String path = new QueryStringDecoder(request.uri()).path();

        switch(path) {
            case OAUTH_PATH:
                processOAuthRequest(ctx, request);
                break;
            default:
                if (hasValidToken(ctx, request)) {
                    forwardRequest(ctx, request);
                }
        }
    }

    private boolean hasValidToken(ChannelHandlerContext ctx, FullHttpRequest request) {
        boolean isAuthorized = false;
        if (request.headers().contains(HttpHeaderNames.AUTHORIZATION)) {
            String encodedAuthHeader = request.headers().get(HttpHeaderNames.AUTHORIZATION);
            OAuthToken receivedOAuthToken = OAuthToken.fromEncodedAuthorizationHeader(encodedAuthHeader);
            if (!receivedOAuthToken.hasValidTokenType()) {
                sendError(ctx, HttpResponseStatus.BAD_REQUEST, "invalid_grant", "Bearer token type required");
            } else if (!issuedTokens.containsKey(receivedOAuthToken.accessToken)) {
                sendError(ctx, HttpResponseStatus.BAD_REQUEST, "invalid_grant", "Invalid token");
            } else if (issuedTokens.get(receivedOAuthToken.accessToken).isExpired()) { // use saved token to check expiration
                sendError(ctx, HttpResponseStatus.BAD_REQUEST, "Token expired");
            } else {
                isAuthorized = true;
            }
        } else {
            sendError(ctx, HttpResponseStatus.BAD_REQUEST, "invalid_request", "authorization header required");
        }
        return isAuthorized;
    }

    private void forwardRequest(ChannelHandlerContext ctx, FullHttpRequest request) {
        ReferenceCountUtil.retain(request);
        ctx.fireChannelRead(request);
    }

    private void processOAuthRequest(ChannelHandlerContext ctx, FullHttpRequest request)
            throws IOException {
        if (request.method() != HttpMethod.POST) {
            sendError(ctx, HttpResponseStatus.BAD_REQUEST, "invalid_request", "oauth request must be POST");
        } else {
            HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(request);
            String grantType = getValue(decoder, GRANT_TYPE_KEY);
            if (grantType == null) {
                String errorDescription = "oauth request must specify grant_type";
                sendError(ctx, HttpResponseStatus.BAD_REQUEST, "invalid_request", errorDescription);
            } else {
                switch (grantType) {
                    case GRANT_TYPE_PASSWORD:
                        processPasswordRequest(ctx, decoder);
                        break;
                    case GRANT_TYPE_REFRESH_TOKEN:
                        processRefreshRequest(ctx, decoder);
                        break;
                    default:
                        String errorDescription = "oauth request must specify grant_type of password or refresh_token";
                        sendError(ctx, HttpResponseStatus.BAD_REQUEST, "invalid_request", errorDescription);
                }
            }
        }
    }

    private void processPasswordRequest(ChannelHandlerContext ctx,
                                        HttpPostRequestDecoder decoder) throws IOException {
                                        //Map<String, List<String>> params) {

        String username = getValue(decoder, USERNAME_KEY);
        String password = getValue(decoder, PASSWORD_KEY);
        if (username == null) {
            String errorDescription = "oauth request must specify username";
            sendError(ctx, HttpResponseStatus.BAD_REQUEST, "invalid_request", errorDescription);
        } else if (password == null) {
            String errorDescription = "oauth request must specify password";
            sendError(ctx, HttpResponseStatus.BAD_REQUEST, "invalid_request", errorDescription);
        } else if (isUserShady(username, password)) {
            sendError(ctx, HttpResponseStatus.BAD_REQUEST, "invalid_client", "user is shady");
        } else {
            respondWithNewOAuthToken(ctx);
        }
    }

    private String getValue(HttpPostRequestDecoder decoder, String key) throws IOException {
        InterfaceHttpData bodyHttpDataForKey = decoder.getBodyHttpData(key);
        if (bodyHttpDataForKey != null && bodyHttpDataForKey instanceof Attribute) {
            return ((Attribute) bodyHttpDataForKey).getValue();
        }
        return null;
    }

    private void processRefreshRequest(ChannelHandlerContext ctx,
                                       HttpPostRequestDecoder decoder) throws IOException {
        String refreshToken = getValue(decoder, REFRESH_TOKEN_KEY);
        if (refreshToken == null) {
            sendError(ctx, HttpResponseStatus.BAD_REQUEST, "invalid_request", "no refresh_token provided");
        } else if (!issuedRefreshTokens.containsKey(refreshToken)) {
            sendError(ctx, HttpResponseStatus.BAD_REQUEST, "invalid_request", "invalid refresh_token provided");
        } else {
            invalidateAssociatedOAuthToken(refreshToken);
            respondWithNewOAuthToken(ctx);
        }
    }

    private void respondWithNewOAuthToken(ChannelHandlerContext ctx) {
        OAuthToken newToken = OAuthToken.generateNew();
        issuedTokens.put(newToken.accessToken, newToken);
        issuedRefreshTokens.put(newToken.refreshToken, newToken);
        respondWithOAuthToken(ctx, newToken);
    }

    private void invalidateAssociatedOAuthToken(String refreshToken) {
        OAuthToken oldToken = issuedRefreshTokens.get(refreshToken);
        issuedTokens.remove(oldToken.accessToken);
        issuedRefreshTokens.remove(oldToken.refreshToken);
    }

    private void respondWithOAuthToken(ChannelHandlerContext ctx, OAuthToken token) {
        String body = GSON.toJson(token);

        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                                                                HttpResponseStatus.OK,
                                                                Unpooled.copiedBuffer(body, RESPONSE_CHARSET));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=UTF-8");
        response.headers().set(HttpHeaderNames.DATE, getDate());
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, body.length());

        ctx.writeAndFlush(response);
    }
    private boolean isUserShady(String username, String password) {
        return "sleepynate".equals(username);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
