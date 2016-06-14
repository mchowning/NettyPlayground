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

    private final String host;
    private final int port;

    protected abstract ChannelHandler[] getChannelHandlers();
    protected abstract Supplier<T> getResultSupplier();
    protected abstract void makeRequest(ChannelOutboundInvoker ctx);

    public Call(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void execute(Consumer<T> resultConsumer) {
        ChannelFutureListener completionListener = ignored ->
                resultConsumer.accept(getResultSupplier().get());
        startClient(completionListener);
    }

    private void startClient(ChannelFutureListener completionListener) {

        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ChannelFuture f = bootstrap(workerGroup).connect(host, port)
                                                    .sync();
            f.channel().closeFuture().addListener(completionListener);
            makeRequest(f.channel());
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
                        ch.pipeline().addLast(getChannelHandlers());
                    }
                });
    }
}
