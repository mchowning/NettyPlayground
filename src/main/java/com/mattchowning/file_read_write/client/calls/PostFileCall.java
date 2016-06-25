package com.mattchowning.file_read_write.client.calls;

import com.mattchowning.file_read_write.client.FileReadWriteClient;
import com.mattchowning.file_read_write.client.handler.HandlerCallback;
import com.mattchowning.file_read_write.server.model.OAuthToken;

import org.jetbrains.annotations.NotNull;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelOutboundInvoker;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

public class PostFileCall extends FileCall {

    private final String newFileContent;

    public PostFileCall(OAuthToken oAuthToken,
                        @NotNull String newFileContent,
                        FileReadWriteClient client,
                        HandlerCallback<String> callback) {
        super(oAuthToken, client, callback);
        this.newFileContent = newFileContent;
    }

    @Override
    public void execute() {
        System.out.println("Requesting to post file content...");
        super.execute();
    }

    @Override
    protected FullHttpMessage getRequest(ChannelOutboundInvoker ctx) {
        System.out.println("Posting updated file content...");
        FullHttpMessage message = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1,
                                                             HttpMethod.POST,
                                                             "",
                                                             Unpooled.copiedBuffer(newFileContent,
                                                                                   CharsetUtil.UTF_8));
        message.headers().add(HttpHeaderNames.CONTENT_LENGTH, newFileContent.length());
        return message;
    }
}
