package com.mattchowning.file_read_write.client.calls;

import com.mattchowning.file_read_write.SharedConstants;
import com.mattchowning.file_read_write.client.FileReadWriteClient;
import com.mattchowning.file_read_write.client.handler.ReadInboundFileClientHandler;
import com.mattchowning.file_read_write.server.model.OAuthToken;

import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOutboundInvoker;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;

public abstract class FileCall extends Call<String> {

    protected OAuthToken oAuthToken;
    private final FileReadWriteClient client;
    private final ChannelHandler[] handlers;
    private final Supplier<String> resultSupplier;

    protected abstract void makeAuthenticatedRequest(ChannelOutboundInvoker ctx);

    public FileCall(@NotNull OAuthToken oAuthToken, FileReadWriteClient client) {
        super(SharedConstants.FILE_HOST, SharedConstants.FILE_PORT);
        this.oAuthToken = oAuthToken;
        this.client = client;
        ReadInboundFileClientHandler readInboundFileClientHandler = new ReadInboundFileClientHandler();
        handlers = new ChannelHandler[] { new HttpClientCodec(),
                                          new HttpObjectAggregator(MAX_BODY_LENGTH),
                                          readInboundFileClientHandler };
        resultSupplier = readInboundFileClientHandler::getFileContent;
    }

    @Override
    protected void makeRequest(ChannelOutboundInvoker ctx) {
        if (oAuthToken.isExpired()) {
            System.out.println("OAuth token expired.");
            client.refreshOAuthToken(refreshedOAuthToken -> {
                this.oAuthToken = refreshedOAuthToken;
                makeAuthenticatedRequest(ctx);
            });
        } else {
            makeAuthenticatedRequest(ctx);
        }
    }

    @Override
    protected ChannelHandler[] getChannelHandlers() {
        return handlers;
    }

    @Override
    protected Supplier<String> getResultSupplier() {
        return resultSupplier;
    }
}
