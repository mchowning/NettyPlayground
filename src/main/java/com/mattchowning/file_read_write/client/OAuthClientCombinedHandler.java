package com.mattchowning.file_read_write.client;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.CombinedChannelDuplexHandler;

@ChannelHandler.Sharable
public class OAuthClientCombinedHandler extends CombinedChannelDuplexHandler<InitialAuthHandler, OAuthClientOutboundHandler> {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    protected OAuthClientCombinedHandler() {
        super();
        OAuthClientOutboundHandler outboundHandler = new OAuthClientOutboundHandler();
        InitialAuthHandler inboundHandler = new InitialAuthHandler(outboundHandler);
        init(inboundHandler, outboundHandler);

    }
}
