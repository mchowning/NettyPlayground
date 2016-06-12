package com.mattchowning.file_read_write.client.calls;

import com.mattchowning.file_read_write.client.FileReadWriteClient;
import com.mattchowning.file_read_write.server.model.OAuthToken;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelOutboundInvoker;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpMessage;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;

public class PostFileCall extends FileCall {

    private final String newFileContent;

    public PostFileCall(@NotNull OAuthToken oAuthToken,
                        @NotNull String newFileContent,
                        FileReadWriteClient client) {
        super(oAuthToken, client);
        this.newFileContent = newFileContent;
    }

    @Override
    public void execute(@NotNull Consumer<String> resultConsumer) {
        System.out.println("Requesting to post file content...");
        super.execute(resultConsumer);
    }

    @Override
    protected void makeAuthenticatedRequest(ChannelOutboundInvoker ctx) {
        FullHttpMessage message = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1,
                                                             HttpMethod.POST,
                                                             "",
                                                             Unpooled.copiedBuffer(newFileContent,
                                                                                   CharsetUtil.UTF_8));
        message.headers()
               .add(HttpHeaderNames.CONTENT_LENGTH, newFileContent.length())
               .add(HttpHeaderNames.AUTHORIZATION, oAuthToken.getEncodedAuthorizationHeader());
        System.out.println("Posting updated file content...");
        ctx.writeAndFlush(message);
    }
}
