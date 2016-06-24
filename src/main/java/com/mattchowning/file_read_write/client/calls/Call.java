package com.mattchowning.file_read_write.client.calls;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public abstract class Call<T> {

    protected static final int MAX_BODY_LENGTH = 15000;

    private final String host;
    private final int port;

    protected abstract ChannelHandler[] getChannelHandlers();
    protected abstract void makeRequest(ChannelOutboundInvoker ctx);

    public Call(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void execute() {

        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ChannelFuture f = bootstrap(workerGroup).connect(host, port)
                                                    .sync();
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
