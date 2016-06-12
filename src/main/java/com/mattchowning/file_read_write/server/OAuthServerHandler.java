package com.mattchowning.file_read_write.server;

import com.mattchowning.file_read_write.server.model.OAuthToken;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.QueryStringDecoder;
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

    private void processOAuthRequest(ChannelHandlerContext ctx, FullHttpRequest request) {
        Map<String, List<String>> params = new QueryStringDecoder(request.uri()).parameters();
        if (request.method() == HttpMethod.POST) {
            if (params.containsKey(GRANT_TYPE_KEY)) {
                if (hasParam(params, GRANT_TYPE_KEY, GRANT_TYPE_PASSWORD)) {
                    processPasswordRequest(ctx, params);
                } else if (hasParam(params, GRANT_TYPE_KEY, GRANT_TYPE_REFRESH_TOKEN)) {
                    processRefreshRequest(ctx, params);
                } else {
                    String errorDescription = "oauth request must specify grant_type of password or refresh_token";
                    sendError(ctx, HttpResponseStatus.BAD_REQUEST, "invalid_request", errorDescription);
                }
            } else {
                String errorDescription = "oauth request must specify grant_type";
                sendError(ctx, HttpResponseStatus.BAD_REQUEST, "invalid_request", errorDescription);
            }
        } else {
            sendError(ctx, HttpResponseStatus.BAD_REQUEST, "invalid_request", "oauth request must be POST");
        }
    }

    private void processPasswordRequest(ChannelHandlerContext ctx,
                                        Map<String, List<String>> params) {
        if (!hasParamKey(params, USERNAME_KEY)) {
            String errorDescription = "oauth request must specify username";
            sendError(ctx, HttpResponseStatus.BAD_REQUEST, "invalid_request", errorDescription);
        } else if (!hasParamKey(params, PASSWORD_KEY)) {
            String errorDescription = "oauth request must specify password";
            sendError(ctx, HttpResponseStatus.BAD_REQUEST, "invalid_request", errorDescription);
        } else if (isUserShady(params)) {
            sendError(ctx, HttpResponseStatus.BAD_REQUEST, "invalid_client", "user is shady");
        }  else {
            respondWithNewOAuthToken(ctx);
        }
    }

    private void processRefreshRequest(ChannelHandlerContext ctx,
                                       Map<String, List<String>> params) {
        if (params.containsKey(REFRESH_TOKEN_KEY)) {
            String refreshTokenInRequest = params.get(REFRESH_TOKEN_KEY).get(0);
            if (issuedRefreshTokens.containsKey(refreshTokenInRequest)) {
                invalidateAssociatedOAuthToken(refreshTokenInRequest);
                respondWithNewOAuthToken(ctx);
            } else {
                sendError(ctx, HttpResponseStatus.BAD_REQUEST, "invalid_request", "invalid refresh_token provided");
            }
        } else {
            sendError(ctx, HttpResponseStatus.BAD_REQUEST, "invalid_request", "no refresh_token provided");
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

    private boolean hasParamKey(Map<String, List<String>> params, String key) {
        return params != null
               && params.get(key) != null;
    }

    private boolean hasParam(Map<String, List<String>> params, String key, String value) {
        return params != null
               && params.get(key) != null &&
               params.get(key).contains(value);
    }

    private boolean isUserShady(Map<String, List<String>> params) {
        return hasParam(params, USERNAME_KEY, "sleepynate");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
