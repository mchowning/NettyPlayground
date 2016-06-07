package com.mattchowning.file_read_write_client;

import com.mattchowning.file_read_write_server.model.OAuthModel;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpMessage;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;

public class GetFileClientHandler extends SimpleChannelInboundHandler<FullHttpResponse> {

    private final OAuthModel oAuthModel;

    public GetFileClientHandler(OAuthModel oAuthModel) {
        this.oAuthModel = oAuthModel;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        requestFileContent(ctx);
    }

    private void requestFileContent(ChannelHandlerContext ctx) {
        FullHttpMessage message = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "");
        message.headers().add(HttpHeaderNames.AUTHORIZATION, oAuthModel.getEncodedAuthorizationHeader());
        ctx.writeAndFlush(message);
        System.out.println("file content requested");
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