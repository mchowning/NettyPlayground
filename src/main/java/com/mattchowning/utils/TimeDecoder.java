package com.mattchowning.utils;

import com.mattchowning.model.UnixTime;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

// Could further simplify by extending ReplayingDecoder here instead
public class TimeDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() >= 4) {
            out.add(new UnixTime(in.readUnsignedInt()));
        }
    }
}
