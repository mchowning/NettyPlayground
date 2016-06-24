package com.mattchowning.file_read_write.client.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.util.CharsetUtil;

public class ClientReadInboundFileHandler extends SimpleChannelInboundHandler<FullHttpResponse> {

    private final HandlerCallback<String> callback;

    public ClientReadInboundFileHandler(HandlerCallback<String> callback) {
        this.callback = callback;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpResponse response) throws Exception {
        String responseContent = getContent(response);
        if (response.status().code() == 200) {
            callback.onSuccess(responseContent);
        } else {
            System.out.println("Error retrieving file: " + responseContent);
            callback.onError();
        }
        ctx.close();
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
        callback.onError();
    }
}
