package com.mattchowning.file_read_write.server.handler;

import com.mattchowning.file_read_write.server.ServerUtil;
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

public class ServerOAuthRequestHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private final OAuthTokenMap oAuthTokens;
    private final ServerUtil serverUtil;

    public ServerOAuthRequestHandler(OAuthTokenMap oAuthTokenMap) {
        this(oAuthTokenMap, new ServerUtil());
    }

    public ServerOAuthRequestHandler(OAuthTokenMap oAuthTokenMap, ServerUtil serverUtil) {
        super();
        this.oAuthTokens = oAuthTokenMap;
        this.serverUtil = serverUtil;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        if (OAUTH_PATH.equals(request.uri())) {
            processOAuthRequest(ctx, request);
        } else {
            invalidEndpoint(ctx);
        }
    }

    private void processOAuthRequest(ChannelHandlerContext ctx, FullHttpRequest request)
            throws IOException {
        if (request.method() != HttpMethod.POST) {
            serverUtil.sendError(ctx, HttpResponseStatus.BAD_REQUEST, "invalid_request", "oauth request must be POST");
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
                    serverUtil.sendError(ctx, HttpResponseStatus.BAD_REQUEST, "invalid_request", errorDescription);
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
            serverUtil.sendError(ctx, HttpResponseStatus.BAD_REQUEST, "invalid_request", errorDescription);
        } else if (password.isEmpty()) {
            String errorDescription = "oauth request must specify password";
            serverUtil.sendError(ctx, HttpResponseStatus.BAD_REQUEST, "invalid_request", errorDescription);
        } else if (isUserShady(username, password)) {
            serverUtil.sendError(ctx, HttpResponseStatus.BAD_REQUEST, "invalid_client", "user is shady");
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
            serverUtil.sendError(ctx, HttpResponseStatus.BAD_REQUEST, "invalid_request", "valid refresh_token not provided");
        }
    }

    private void respondWithNewOAuthToken(ChannelHandlerContext ctx) {
        OAuthToken newToken = new OAuthToken();
        oAuthTokens.add(newToken);
        respondWithOAuthToken(ctx, newToken);
    }

    private void respondWithOAuthToken(ChannelHandlerContext ctx, OAuthToken token) {
        String body = GSON.toJson(token);

        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                                                                HttpResponseStatus.OK,
                                                                Unpooled.copiedBuffer(body, RESPONSE_CHARSET));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=UTF-8");
        response.headers().set(HttpHeaderNames.DATE, serverUtil.getDate());
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
