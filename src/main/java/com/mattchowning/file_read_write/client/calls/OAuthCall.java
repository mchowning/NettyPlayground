package com.mattchowning.file_read_write.client.calls;

import com.mattchowning.file_read_write.SharedConstants;
import com.mattchowning.file_read_write.client.handler.ClientInitialAuthHandler;
import com.mattchowning.file_read_write.client.handler.HandlerCallback;
import com.mattchowning.file_read_write.server.model.OAuthToken;

import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.http.*;

public abstract class OAuthCall extends Call<OAuthToken> {

    private final ChannelHandler[] handlers;

    public OAuthCall(HandlerCallback<OAuthToken> callback) {
        super(SharedConstants.OAUTH_HOST, SharedConstants.OAUTH_PORT);

        handlers = new ChannelHandler[] {new HttpClientCodec(),
                                         new HttpObjectAggregator(MAX_BODY_LENGTH),
                                         new ClientInitialAuthHandler(callback)};
    }

    @Override
    protected ChannelHandler[] getChannelHandlers() {
        return handlers;
    }
}
