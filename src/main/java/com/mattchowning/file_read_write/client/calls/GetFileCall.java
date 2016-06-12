package com.mattchowning.file_read_write.client.calls;

import com.mattchowning.file_read_write.client.FileReadWriteClient;
import com.mattchowning.file_read_write.server.model.OAuthToken;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

import io.netty.channel.ChannelOutboundInvoker;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpMessage;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;

public class GetFileCall extends FileCall {
                                                                // FIXME
    public GetFileCall(@NotNull OAuthToken oAuthToken, @NotNull FileReadWriteClient client) {
        super(oAuthToken, client);
    }

    @Override
    public void execute(@NotNull Consumer<String> resultConsumer) {
        System.out.println("Requesting file content...");
        super.execute(resultConsumer);
    }

    @Override
    protected void makeAuthenticatedRequest(ChannelOutboundInvoker ctx) {
        FullHttpMessage message = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1,
                                                             HttpMethod.GET,
                                                             "");
        message.headers().add(HttpHeaderNames.AUTHORIZATION,
                              oAuthToken.getEncodedAuthorizationHeader());
        ctx.writeAndFlush(message);
    }
}
