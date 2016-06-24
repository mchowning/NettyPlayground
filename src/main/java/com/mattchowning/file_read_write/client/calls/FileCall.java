package com.mattchowning.file_read_write.client.calls;

import com.mattchowning.file_read_write.SharedConstants;
import com.mattchowning.file_read_write.client.FileReadWriteClient;
import com.mattchowning.file_read_write.client.handler.ClientReadInboundFileHandler;
import com.mattchowning.file_read_write.client.handler.HandlerCallback;
import com.mattchowning.file_read_write.server.model.OAuthToken;

import org.jetbrains.annotations.NotNull;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOutboundInvoker;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;

public abstract class FileCall extends Call<String> {

    protected OAuthToken oAuthToken;
    private final FileReadWriteClient client;
    private final ChannelHandler[] handlers;

    protected abstract void makeAuthenticatedRequest(ChannelOutboundInvoker ctx);

    public FileCall(@NotNull OAuthToken oAuthToken,
                    FileReadWriteClient client,
                    HandlerCallback<String> callback) {
        super(SharedConstants.FILE_HOST, SharedConstants.FILE_PORT);
        this.oAuthToken = oAuthToken;
        this.client = client;
        handlers = new ChannelHandler[] { new HttpClientCodec(),
                                          new HttpObjectAggregator(MAX_BODY_LENGTH),
                                          new ClientReadInboundFileHandler(callback)};
    }

    @Override
    protected void makeRequest(ChannelOutboundInvoker ctx) {
        if (oAuthToken.isExpired()) {
            System.out.println("OAuth token expired.");
            client.refreshOAuthToken(new HandlerCallback<OAuthToken>() {
                @Override
                public void onSuccess(OAuthToken result) {
                    FileCall.this.oAuthToken = result;
                    makeAuthenticatedRequest(ctx);
                }

                @Override
                public void onError() {
                    System.out.println("Error refreshing OAuth token. Trying again...");
                    // Try again?
                }
            });
        } else {
            makeAuthenticatedRequest(ctx);
        }
    }

    @Override
    protected ChannelHandler[] getChannelHandlers() {
        return handlers;
    }
}
