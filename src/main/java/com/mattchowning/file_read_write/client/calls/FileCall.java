package com.mattchowning.file_read_write.client.calls;

import com.mattchowning.file_read_write.SharedConstants;
import com.mattchowning.file_read_write.client.FileReadWriteClient;
import com.mattchowning.file_read_write.client.handler.ClientReadInboundFileHandler;
import com.mattchowning.file_read_write.client.handler.HandlerCallback;
import com.mattchowning.file_read_write.server.model.OAuthToken;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOutboundInvoker;
import io.netty.handler.codec.http.*;

public abstract class FileCall extends Call<String> {

    private OAuthToken oAuthToken;
    private final FileReadWriteClient client;
    private final ChannelHandler[] handlers;

    protected abstract FullHttpMessage getRequest(ChannelOutboundInvoker ctx);

    public FileCall(OAuthToken oAuthToken,
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
        if (oAuthToken == null) {
            System.out.println("Making unauthenticated request");
            makeUnauthenticatedRequest(ctx);
        } else if (oAuthToken.isExpired()) {
            System.out.println("OAuth token expired.");
            client.refreshOAuthToken(getRefreshCallback(ctx));
        } else {
            makeAuthenticatedRequest(ctx);
        }
    }

    private HandlerCallback<OAuthToken> getRefreshCallback(ChannelOutboundInvoker ctx) {
        return new HandlerCallback<OAuthToken>() {
            @Override
            public void onSuccess(OAuthToken result) {
                oAuthToken = result;
                makeAuthenticatedRequest(ctx);
            }

            @Override
            public void onError() {
                System.out.println(
                        "Error refreshing OAuth token. Proceeding without authorization...");
                makeUnauthenticatedRequest(ctx);
            }
        };
    }

    private void makeAuthenticatedRequest(ChannelOutboundInvoker ctx) {
        FullHttpMessage message = getRequest(ctx);
        message.headers().add(HttpHeaderNames.AUTHORIZATION,
                              oAuthToken.getEncodedAuthorizationHeader());
        ctx.writeAndFlush(message);
    }

    private void makeUnauthenticatedRequest(ChannelOutboundInvoker ctx) {
        ctx.writeAndFlush(getRequest(ctx));
    }

    @Override
    protected ChannelHandler[] getChannelHandlers() {
        return handlers;
    }
}
