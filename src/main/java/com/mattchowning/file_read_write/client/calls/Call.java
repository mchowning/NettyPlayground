package com.mattchowning.file_read_write.client.calls;

import java.util.function.Consumer;
import java.util.function.Supplier;

import io.netty.bootstrap.Bootstrap;
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

public abstract class Call<T> {

    protected static final int MAX_BODY_LENGTH = 15000;
    private static final String HOST = "localhost";
    private static final int PORT = 8080;

    protected abstract ChannelHandler[] getChannelHandlers();
    protected abstract Supplier<T> getResultSupplier();
    protected abstract void makeRequest(ChannelOutboundInvoker ctx);

    public void execute(Consumer<T> resultConsumer) {
        ChannelFutureListener completionListener = f -> resultConsumer.accept(getResultSupplier().get());
        startClient(this::makeRequest, getChannelHandlers(), completionListener);
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
}
