package com.mattchowning.file_read_write.client;

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

    private final String requestedNewFileContent;

    public PostFileClientHandler(String requestedNewFileContent) {
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
        message.headers().add(HttpHeaderNames.CONTENT_LENGTH, requestedNewFileContent.length());
        System.out.println("Posting updated file content...");
        ctx.writeAndFlush(message);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpResponse response) throws Exception {
        // FIXME handle file content as json content
        // FIXME this is duplicated in GetFileClientHandler
        if (response.status().code() == 200) {
            String fileText = getContent(response);
            System.out.println("File content received: " + fileText);
        } else {
            System.out.println("Error retrieving file: " + response);
        }
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
