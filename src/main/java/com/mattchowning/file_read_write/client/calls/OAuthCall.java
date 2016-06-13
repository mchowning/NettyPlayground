package com.mattchowning.file_read_write.client.calls;

import com.mattchowning.file_read_write.client.InitialAuthHandler;
import com.mattchowning.file_read_write.server.model.OAuthToken;

import java.util.function.Supplier;

import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.http.*;

public abstract class OAuthCall extends Call<OAuthToken> {

    private final ChannelHandler[] handlers;
    private final Supplier<OAuthToken> resultSupplier;

    public OAuthCall() {

        InitialAuthHandler initialAuthHandler = new InitialAuthHandler();
        handlers = new ChannelHandler[] {new HttpClientCodec(),
                                         new HttpObjectAggregator(MAX_BODY_LENGTH),
                                         initialAuthHandler };
        resultSupplier = initialAuthHandler::getOAuthModel;
    }

    @Override
    protected ChannelHandler[] getChannelHandlers() {
        return handlers;
    }

    @Override
    protected Supplier<OAuthToken> getResultSupplier() {
        return resultSupplier;
    }
}
