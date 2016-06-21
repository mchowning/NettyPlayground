package com.mattchowning_scala.file_read_write.client.calls

import io.netty.bootstrap.Bootstrap
import io.netty.channel._
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel

abstract class Call[T](host: String, port: Int) {
  protected val MAX_BODY_LENGTH: Int = 15000

  protected def makeRequest(ctx: ChannelOutboundInvoker)
  protected val getChannelHandlers: Array[ChannelHandler]

  def execute() = {
    val workerGroup: EventLoopGroup = new NioEventLoopGroup
    try {
      val f: ChannelFuture = bootstrap(workerGroup).connect(host, port).sync
      makeRequest(f.channel)
      f.channel.closeFuture.sync
    }
    catch {
      case e: InterruptedException => e.printStackTrace()
    } finally {
      workerGroup.shutdownGracefully
    }
  }

  private def bootstrap(workerGroup: EventLoopGroup): Bootstrap = {
    new Bootstrap().group(workerGroup)
      .channel(classOf[NioSocketChannel])
      .option[java.lang.Boolean](ChannelOption.SO_KEEPALIVE, true)
      .handler(new ChannelInitializer[SocketChannel]() {
        @throws(classOf[Exception])
        protected def initChannel(ch: SocketChannel) =
          ch.pipeline().addLast(getChannelHandlers:_*)
      })
  }
}