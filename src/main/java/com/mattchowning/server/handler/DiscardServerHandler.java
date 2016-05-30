package com.mattchowning.server.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class DiscardServerHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //((ByteBuf) msg).release(); // true discard
        ByteBuf in = (ByteBuf) msg;
        try {
            while (in.isReadable()) { // inefficient loop
                char c = (char) in.readByte();
                System.out.print(c);
                System.out.flush();
            }
        } finally {
            // ReferenceCountUtil.release(msg);
            in.release();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
