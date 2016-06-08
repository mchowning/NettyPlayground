package com.mattchowning.file_read_write.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpMessage;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;

public class GetFileClientHandler extends SimpleChannelInboundHandler<FullHttpResponse> {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        requestFileContent(ctx);
    }

    private void requestFileContent(ChannelHandlerContext ctx) {
        FullHttpMessage message = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "");
        System.out.println("Requesting file content...");
        ctx.writeAndFlush(message);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpResponse msg) throws Exception {
        // FIXME handle error response
        // FIXME handle file content as json content
        String fileText = getContent(msg);
        System.out.println("File content received: " + fileText);
        //ctx.close();
    }

    private String getContent(FullHttpResponse msg) {
        byte[] msgBytes = new byte[msg.content().capacity()];
        msg.content().getBytes(0, msgBytes);
        return new String(msgBytes, CharsetUtil.UTF_8);
    }

    // TODO Instead of this add a DefaultExceptionHandler to the the pipeline?
    // https://bitbucket.org/adolgarev/nettybackend/src/19b0c41ddafc2921138595fdb750b09ff9668a58/src/main/java/test/backend/http/DefaultExceptionHandler.java?at=master&fileviewer=file-view-default
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
