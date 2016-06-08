package com.mattchowning.file_read_write.server;

import com.mattchowning.file_read_write.server.model.OAuthModel;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

import static com.mattchowning.file_read_write.SharedConstants.GRANT_TYPE_KEY;
import static com.mattchowning.file_read_write.SharedConstants.GRANT_TYPE_PASSWORD;
import static com.mattchowning.file_read_write.SharedConstants.GSON;
import static com.mattchowning.file_read_write.SharedConstants.OAUTH_PATH;
import static com.mattchowning.file_read_write.SharedConstants.PASSWORD_KEY;
import static com.mattchowning.file_read_write.SharedConstants.RESPONSE_CHARSET;
import static com.mattchowning.file_read_write.SharedConstants.USERNAME_KEY;
import static com.mattchowning.file_read_write.server.ServerUtils.getDate;
import static com.mattchowning.file_read_write.server.ServerUtils.sendError;

@ChannelHandler.Sharable
public class OAuthServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private Set<String> issuedTokens = new HashSet<>();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        if (isOAuthRequest(request)) {
            processOAuthRequest(ctx, request);
        } else if (isAuthorized(request)) {
            ReferenceCountUtil.retain(request);
            ctx.fireChannelRead(request);
        } else {
            sendError(ctx, HttpResponseStatus.UNAUTHORIZED, "invalid_client", "client not authorized");
        }

    }

    private boolean isAuthorized(FullHttpRequest request) {
        if (request.headers().contains(HttpHeaderNames.AUTHORIZATION)) {
            String encodedAuthHeader = request.headers().get(HttpHeaderNames.AUTHORIZATION);
            OAuthModel oAuthModel = OAuthModel.fromEncodedAuthorizationHeader(encodedAuthHeader);
            return isAuthorized(oAuthModel);
        }
        return false;
    }

    private boolean isAuthorized(OAuthModel oAuthModel) {
        if (oAuthModel.hasValidTokenType()) {
            return issuedTokens.contains(oAuthModel.accessToken);
        } else {
            throw new RuntimeException("Bearer token type required");
        }
    }

    private void processOAuthRequest(ChannelHandlerContext ctx, FullHttpRequest request) {
        if (checkOAuthRequest(ctx, request)) {
            String newToken = getNewToken();
            issuedTokens.add(newToken);
            respondWithNewOAuthToken(ctx, newToken);
        }
    }

    private boolean checkOAuthRequest(ChannelHandlerContext ctx, FullHttpRequest request) {
        boolean isApproved = false;
        Map<String, List<String>> params = new QueryStringDecoder(request.uri()).parameters();
        if (request.method() != HttpMethod.POST) {
            sendError(ctx, HttpResponseStatus.BAD_REQUEST, "invalid_request", "oauth request must be POST");
        } else if (!hasParam(params, GRANT_TYPE_KEY, GRANT_TYPE_PASSWORD)) {
            String errorDescription = "oauth request must specify grant_type of password";
            sendError(ctx, HttpResponseStatus.BAD_REQUEST, "invalid_request", errorDescription);
        } else if (!hasParamKey(params, USERNAME_KEY)) {
            String errorDescription = "oauth request must specify username";
            sendError(ctx, HttpResponseStatus.BAD_REQUEST, "invalid_request", errorDescription);
        } else if (!hasParamKey(params, PASSWORD_KEY)) {
            String errorDescription = "oauth request must specify password";
            sendError(ctx, HttpResponseStatus.BAD_REQUEST, "invalid_request", errorDescription);
        } else if (isShadyUser(params)) {
            sendError(ctx, HttpResponseStatus.BAD_REQUEST, "invalid_client", "user is shady");
        } else {
            isApproved = true;
        }
        return isApproved;
    }

    private void respondWithNewOAuthToken(ChannelHandlerContext ctx, String token) {
        String body = GSON.toJson(new OAuthModel(token));

        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                                                                HttpResponseStatus.OK,
                                                                Unpooled.copiedBuffer(body, RESPONSE_CHARSET));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=UTF-8");
        response.headers().set(HttpHeaderNames.DATE, getDate());
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, body.length());

        ctx.writeAndFlush(response);
    }

    private String getNewToken() {
        String token = TokenGenerator.generateNew();
        issuedTokens.add(token);
        return token;
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

    private boolean isShadyUser(Map<String, List<String>> params) {
        return hasParam(params, USERNAME_KEY, "sleepynate");
    }

    private boolean isOAuthRequest(FullHttpRequest request) {
        if (request != null) {
            String path = new QueryStringDecoder(request.uri()).path();
            return OAUTH_PATH.equals(path);
        } else {
            return false;
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
