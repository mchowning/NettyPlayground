package com.mattchowning.file_read_write.client;

import java.util.function.Consumer;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
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
import io.netty.util.CharsetUtil;

public class FileReadWriteClient {

    interface CompletionListener {
        void onCompleted();
    }

    private static final int MAX_BODY_LENGTH = 15000;
    private static final int PORT = 8080;
    private static final String HOST = "localhost";

    private final CompletionListener completionListener;
    private final OAuthClientCombinedHandler oAuthClientCombinedHandler;

    public FileReadWriteClient(CompletionListener completionListener) {
        this.completionListener = completionListener;
        oAuthClientCombinedHandler = new OAuthClientCombinedHandler();
    }

    void retrieveFileContent() {
        startClient(this::retrieveFileCall);
    }

    void updateFileContent(String newFileContent) {
        Consumer<ChannelOutboundInvoker> httpCall = postFileCall(newFileContent);
        startClient(httpCall);
    }

    private void startClient(Consumer<ChannelOutboundInvoker> httpCall) {

        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ChannelFuture f = bootstrap(workerGroup).connect(HOST, PORT)
                                                    .sync();
            f.channel().closeFuture().addListener(future -> completionListener.onCompleted());
            httpCall.accept(f.channel());
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            workerGroup.shutdownGracefully();
        }
    }

    private Bootstrap bootstrap(EventLoopGroup workerGroup) {
        return new Bootstrap()
                .group(workerGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new HttpClientCodec(),
                                              new HttpObjectAggregator(MAX_BODY_LENGTH),
                                              oAuthClientCombinedHandler,
                                              new ReadInboundFileClientHandler());
                    }
                });
    }

    private void retrieveFileCall(ChannelOutboundInvoker ctx) {
        System.out.println("Requesting file content...");
        FullHttpMessage message = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "");
        ctx.writeAndFlush(message);
    }

    private Consumer<ChannelOutboundInvoker> postFileCall(String newFileContent) {
        return ctx -> {
            FullHttpMessage message = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1,
                                                                 HttpMethod.POST,
                                                                 "",
                                                                 Unpooled.copiedBuffer(newFileContent, CharsetUtil.UTF_8));
            message.headers().add(HttpHeaderNames.CONTENT_LENGTH, newFileContent.length());
            System.out.println("Posting updated file content...");
            ctx.writeAndFlush(message);
        };
    }
}
