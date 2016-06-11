package com.mattchowning.file_read_write.client.calls;

import com.mattchowning.file_read_write.client.ReadInboundFileClientHandler;
import com.mattchowning.file_read_write.server.model.OAuthModel;

import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;

public abstract class FileCall extends Call<String> {

    protected final OAuthModel oAuthModel;
    private final ChannelHandler[] handlers;
    private final Supplier<String> resultSupplier;

    public FileCall(@NotNull OAuthModel oAuthModel) {
        this.oAuthModel = oAuthModel;
        ReadInboundFileClientHandler readInboundFileClientHandler = new ReadInboundFileClientHandler();
        handlers = new ChannelHandler[] { new HttpClientCodec(),
                                          new HttpObjectAggregator(MAX_BODY_LENGTH),
                                          readInboundFileClientHandler };
        resultSupplier = readInboundFileClientHandler::getFileContent;
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
