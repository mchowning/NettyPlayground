package com.mattchowning.file_read_write.server;

import com.mattchowning.file_read_write.server.model.HttpError;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;

import static com.mattchowning.file_read_write.SharedConstants.GSON;
import static com.mattchowning.file_read_write.SharedConstants.RESPONSE_CHARSET;

public class ServerUtils {

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
