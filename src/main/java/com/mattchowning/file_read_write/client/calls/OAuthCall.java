package com.mattchowning.file_read_write.client.calls;

import com.mattchowning.file_read_write.SharedConstants;
import com.mattchowning.file_read_write.client.handler.ClientInitialAuthHandler;
import com.mattchowning.file_read_write.server.model.OAuthToken;

import java.util.function.Supplier;

import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.http.*;

public abstract class OAuthCall extends Call<OAuthToken> {

    private final ChannelHandler[] handlers;
    private final Supplier<OAuthToken> resultSupplier;

    public OAuthCall() {
        super(SharedConstants.OAUTH_HOST, SharedConstants.OAUTH_PORT);

        ClientInitialAuthHandler initialAuthHandler = new ClientInitialAuthHandler();
        handlers = new ChannelHandler[] {new HttpClientCodec(),
                                         new HttpObjectAggregator(MAX_BODY_LENGTH),
                                         initialAuthHandler };
        resultSupplier = initialAuthHandler::getOAuthToken;
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
