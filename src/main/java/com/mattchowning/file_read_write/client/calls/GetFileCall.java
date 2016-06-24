package com.mattchowning.file_read_write.client.calls;

import com.mattchowning.file_read_write.client.FileReadWriteClient;
import com.mattchowning.file_read_write.client.handler.HandlerCallback;
import com.mattchowning.file_read_write.server.model.OAuthToken;

import org.jetbrains.annotations.NotNull;

import io.netty.channel.ChannelOutboundInvoker;
import io.netty.handler.codec.http.*;

public class GetFileCall extends FileCall {

    public GetFileCall(@NotNull OAuthToken oAuthToken,
                       @NotNull FileReadWriteClient client,
                       HandlerCallback<String> callback) {
        super(oAuthToken, client, callback);
    }

    @Override
    public void execute() {
        System.out.println("Requesting file content...");
        super.execute();
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
