package com.mattchowning.file_read_write.server;

import com.mattchowning.file_read_write.server.model.HttpError;
import com.mattchowning.file_read_write.server.model.OAuthToken;

import org.jetbrains.annotations.NotNull;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

import static com.mattchowning.file_read_write.SharedConstants.GSON;
import static com.mattchowning.file_read_write.SharedConstants.RESPONSE_CHARSET;

public class ServerUtil {

    public OAuthToken getOAuthToken(String encodedAuthorizationHeader) {
        String tokenType = getTokenType(encodedAuthorizationHeader);
        String token = getAccessToken(encodedAuthorizationHeader);
        return new OAuthToken(token, tokenType);
    }

    @NotNull
    private static String getAccessToken(@NotNull String authorizationHeader) {
        String[] authHeaderArray = authorizationHeader.split("\\s");
        if (authHeaderArray.length < 2) {
            return "";
        } else {
            String encodedToken = authHeaderArray[1];
            byte[] decodedTokenBytes = Base64.getDecoder().decode(encodedToken);
            return new String(decodedTokenBytes, CharsetUtil.UTF_8);
        }
    }

    @NotNull
    private static String getTokenType(@NotNull String encodedAuthorizationHeader) {
        String[] authHeaderElements = encodedAuthorizationHeader.split("\\s");
        return authHeaderElements[0];
    }

    public String getDate() {
        return DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now());
    }

    public void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
        sendError(ctx, status, status.toString());
    }

    public void sendError(ChannelHandlerContext ctx, HttpResponseStatus status, String errorMsg) {
        sendError(ctx, status, errorMsg, null);
    }

    public void sendError(ChannelHandlerContext ctx,
                                 HttpResponseStatus status,
                                 String errorMsg,
                                 String errorDescription) {
        HttpError error = new HttpError(errorMsg, errorDescription);
        String json = GSON.toJson(error);
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                                                                status,
                                                                Unpooled.copiedBuffer(json, RESPONSE_CHARSET));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
        HttpUtil.setContentLength(response, json.length());
        ctx.writeAndFlush(response);
    }
}
