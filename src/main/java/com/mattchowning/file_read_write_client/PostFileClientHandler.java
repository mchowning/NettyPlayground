package com.mattchowning.file_read_write_client;

import com.mattchowning.file_read_write_server.model.OAuthModel;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpMessage;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;

public class PostFileClientHandler extends SimpleChannelInboundHandler<FullHttpResponse> {

    private final OAuthModel oAuthModel;
    private final String requestedNewFileContent;

    public PostFileClientHandler(OAuthModel oAuthModel, String requestedNewFileContent) {
        this.oAuthModel = oAuthModel;
        this.requestedNewFileContent = requestedNewFileContent;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        postUpdatedFileContent(ctx);
    }

    private void postUpdatedFileContent(ChannelHandlerContext ctx) {
        FullHttpMessage message = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1,
                                                             HttpMethod.POST,
                                                             "",
                                                             Unpooled.copiedBuffer(requestedNewFileContent, CharsetUtil.UTF_8));
        message.headers().add(HttpHeaderNames.AUTHORIZATION, oAuthModel.getEncodedAuthorizationHeader());
        message.headers().add(HttpHeaderNames.CONTENT_LENGTH, requestedNewFileContent.length());
        ctx.writeAndFlush(message);
        System.out.println("file content posted: " + requestedNewFileContent);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpResponse msg) throws Exception {
        // FIXME handle error response
        // FIXME handle file content as json content
        String fileText = getContent(msg);
        System.out.println("file content received: " + fileText);
        ctx.close();
    }

    private String getContent(FullHttpResponse msg) {
        byte[] msgBytes = new byte[msg.content().capacity()];
        msg.content().getBytes(0, msgBytes);
        return new String(msgBytes, CharsetUtil.UTF_8);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
