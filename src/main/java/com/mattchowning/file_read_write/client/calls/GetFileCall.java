package com.mattchowning.file_read_write.client.calls;

import com.mattchowning.file_read_write.client.FileReadWriteClient;
import com.mattchowning.file_read_write.client.handler.HandlerCallback;
import com.mattchowning.file_read_write.server.model.OAuthToken;

import org.jetbrains.annotations.NotNull;

import io.netty.channel.ChannelOutboundInvoker;
import io.netty.handler.codec.http.*;

public class GetFileCall extends FileCall {

    public GetFileCall(OAuthToken oAuthToken,
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
    protected FullHttpMessage getRequest(ChannelOutboundInvoker ctx) {
        return new DefaultFullHttpRequest(HttpVersion.HTTP_1_1,
                                          HttpMethod.GET,
                                          "");
    }
}
