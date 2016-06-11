package com.mattchowning.file_read_write.client;

import com.mattchowning.file_read_write.server.model.OAuthModel;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelOutboundInvoker;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpMessage;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.QueryStringEncoder;
import io.netty.util.CharsetUtil;

import static com.mattchowning.file_read_write.SharedConstants.GRANT_TYPE_KEY;
import static com.mattchowning.file_read_write.SharedConstants.GRANT_TYPE_PASSWORD;
import static com.mattchowning.file_read_write.SharedConstants.OAUTH_PATH;
import static com.mattchowning.file_read_write.SharedConstants.PASSWORD_KEY;
import static com.mattchowning.file_read_write.SharedConstants.USERNAME_KEY;

public class FileReadWriteClient {

    private static final int MAX_BODY_LENGTH = 15000;
    private static final int PORT = 8080;
    private static final String HOST = "localhost";

    private static OAuthModel oAuthModel;

    void retrieveFileContent(@NotNull Consumer<String> consumer) {
        ReadInboundFileClientHandler readInboundFileClientHandler = new ReadInboundFileClientHandler();
        ChannelHandler[] handlers = new ChannelHandler[] { new HttpClientCodec(),
                                                           new HttpObjectAggregator(MAX_BODY_LENGTH),
                                                           readInboundFileClientHandler };
        ChannelFutureListener completionListener =
                f -> consumer.accept(readInboundFileClientHandler.getFileContent());
        startClient(this::retrieveFileCall, handlers, completionListener);
    }

    void updateFileContent(@NotNull String newFileContent, @NotNull Consumer<String> consumer) {
        ReadInboundFileClientHandler readInboundFileClientHandler = new ReadInboundFileClientHandler();
        ChannelHandler[] handlers = new ChannelHandler[] { new HttpClientCodec(),
                                                           new HttpObjectAggregator(MAX_BODY_LENGTH),
                                                           readInboundFileClientHandler };
        Consumer<ChannelOutboundInvoker> httpCall = postFileCall(newFileContent);
        ChannelFutureListener completionListener =
                f -> consumer.accept(readInboundFileClientHandler.getFileContent());
        startClient(httpCall, handlers, completionListener);
    }

    void retrieveOAuthToken(Consumer<OAuthModel> consumer,
                            String username,
                            String password) {
        InitialAuthHandler initialAuthHandler = new InitialAuthHandler();
        ChannelHandler[] handlers = new ChannelHandler[]{ new HttpClientCodec(),
                                                          new HttpObjectAggregator(MAX_BODY_LENGTH),
                                                          initialAuthHandler };
        Consumer<ChannelOutboundInvoker> httpCall = retrieveOAuthTokenCall(username, password);
        ChannelFutureListener completionListener = future -> {
            oAuthModel = initialAuthHandler.getOAuthModel();
            consumer.accept(oAuthModel);
        };
        startClient(httpCall, handlers, completionListener);
    }

    private void startClient(Consumer<ChannelOutboundInvoker> httpCall,
                             ChannelHandler[] handlers,
                             ChannelFutureListener completionListener) {

        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ChannelFuture f = bootstrap(workerGroup, handlers).connect(HOST, PORT)
                                                    .sync();
            f.channel().closeFuture().addListener(completionListener);
            httpCall.accept(f.channel());
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            workerGroup.shutdownGracefully();
        }
    }

    private Bootstrap bootstrap(EventLoopGroup workerGroup, ChannelHandler[] handlers) {
        return new Bootstrap()
                .group(workerGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(handlers);
                    }
                });
    }

    private Consumer<ChannelOutboundInvoker> retrieveOAuthTokenCall(String username, String password) {
        return ctx -> {
            FullHttpMessage message = getOAuthTokenRequest(username, password);
            System.out.println("Requesting OAuth token...");
            ctx.writeAndFlush(message);
        };
    }

    private FullHttpMessage getOAuthTokenRequest(String username, String password) {
        QueryStringEncoder queryStringEncoder = new QueryStringEncoder(OAUTH_PATH);
        queryStringEncoder.addParam(GRANT_TYPE_KEY, GRANT_TYPE_PASSWORD);
        queryStringEncoder.addParam(USERNAME_KEY, username);
        queryStringEncoder.addParam(PASSWORD_KEY, password);
        String uriString = queryStringEncoder.toString();
        return new DefaultFullHttpRequest(HttpVersion.HTTP_1_1,
                                          HttpMethod.POST,
                                          uriString);
    }

    private void retrieveFileCall(ChannelOutboundInvoker ctx) {
        System.out.println("Requesting file content...");
        FullHttpMessage message = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1,
                                                             HttpMethod.GET,
                                                             "");
        message.headers().add(HttpHeaderNames.AUTHORIZATION,
                              oAuthModel.getEncodedAuthorizationHeader());
        ctx.writeAndFlush(message);
    }

    private Consumer<ChannelOutboundInvoker> postFileCall(String newFileContent) {
        return ctx -> {
            FullHttpMessage message = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1,
                                                                 HttpMethod.POST,
                                                                 "",
                                                                 Unpooled.copiedBuffer(newFileContent,
                                                                                       CharsetUtil.UTF_8));
            message.headers()
                   .add(HttpHeaderNames.CONTENT_LENGTH, newFileContent.length())
                   .add(HttpHeaderNames.AUTHORIZATION, oAuthModel.getEncodedAuthorizationHeader());
            System.out.println("Posting updated file content...");
            ctx.writeAndFlush(message);
        };
    }
}
