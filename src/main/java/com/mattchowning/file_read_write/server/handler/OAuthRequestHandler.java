package com.mattchowning.file_read_write.server.handler;

import com.mattchowning.file_read_write.server.model.OAuthToken;
import com.mattchowning.file_read_write.server.model.OAuthTokenMap;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.*;

import static com.mattchowning.file_read_write.SharedConstants.*;
import static com.mattchowning.file_read_write.server.ServerUtils.getDate;
import static com.mattchowning.file_read_write.server.ServerUtils.sendError;

public class OAuthRequestHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private OAuthTokenMap oAuthTokens;

    public OAuthRequestHandler(OAuthTokenMap oAuthTokenMap) {
        super();
        this.oAuthTokens = oAuthTokenMap;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        String path = new QueryStringDecoder(request.uri()).path();
        if (OAUTH_PATH.equals(path)) {
            processOAuthRequest(ctx, request);
        } else {
            invalidEndpoint(ctx);
        }
    }

    private void processOAuthRequest(ChannelHandlerContext ctx, FullHttpRequest request)
            throws IOException {
        if (request.method() != HttpMethod.POST) {
            sendError(ctx, HttpResponseStatus.BAD_REQUEST, "invalid_request", "oauth request must be POST");
        } else {
            HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(request);
            String grantType = getValue(decoder, GRANT_TYPE_KEY);
            switch (grantType) {
                case GRANT_TYPE_PASSWORD:
                    processPasswordRequest(ctx, decoder);
                    break;
                case GRANT_TYPE_REFRESH_TOKEN:
                    processRefreshRequest(ctx, decoder);
                    break;
                default:
                    String errorDescription = String.format("oauth request must specify grant_type of %s or %s",
                                                            GRANT_TYPE_PASSWORD,
                                                            GRANT_TYPE_REFRESH_TOKEN);
                    sendError(ctx, HttpResponseStatus.BAD_REQUEST, "invalid_request", errorDescription);
            }
        }
    }

    @NotNull
    private String getValue(HttpPostRequestDecoder decoder, String key) throws IOException {
        String result = null;
        InterfaceHttpData bodyHttpDataForKey = decoder.getBodyHttpData(key);
        if (bodyHttpDataForKey != null && bodyHttpDataForKey instanceof Attribute) {
            result = ((Attribute) bodyHttpDataForKey).getValue();
        }
        if (result == null) result = "";
        return result;
    }

    private void processPasswordRequest(ChannelHandlerContext ctx,
                                        HttpPostRequestDecoder decoder) throws IOException {

        String username = getValue(decoder, USERNAME_KEY);
        String password = getValue(decoder, PASSWORD_KEY);
        if (username.isEmpty()) {
            String errorDescription = "oauth request must specify username";
            sendError(ctx, HttpResponseStatus.BAD_REQUEST, "invalid_request", errorDescription);
        } else if (password.isEmpty()) {
            String errorDescription = "oauth request must specify password";
            sendError(ctx, HttpResponseStatus.BAD_REQUEST, "invalid_request", errorDescription);
        } else if (isUserShady(username, password)) {
            sendError(ctx, HttpResponseStatus.BAD_REQUEST, "invalid_client", "user is shady");
        } else {
            respondWithNewOAuthToken(ctx);
        }
    }

    private void processRefreshRequest(ChannelHandlerContext ctx,
                                       HttpPostRequestDecoder decoder) throws IOException {
        String refreshToken = getValue(decoder, REFRESH_TOKEN_KEY);
        if (oAuthTokens.containsRefreshToken(refreshToken)) {
            oAuthTokens.removeToken(oAuthTokens.getWithRefreshToken(refreshToken));
            respondWithNewOAuthToken(ctx);
        } else {
            sendError(ctx, HttpResponseStatus.BAD_REQUEST, "invalid_request", "valid refresh_token not provided");
        }
    }

    private void respondWithNewOAuthToken(ChannelHandlerContext ctx) {
        OAuthToken newToken = OAuthToken.generateNew();
        oAuthTokens.add(newToken);
        respondWithOAuthToken(ctx, newToken);
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

    private static void invalidEndpoint(ChannelHandlerContext ctx) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                                                                HttpResponseStatus.NOT_FOUND);
        ctx.writeAndFlush(response);
    }
}
