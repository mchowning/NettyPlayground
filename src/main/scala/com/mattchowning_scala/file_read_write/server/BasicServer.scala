package com.mattchowning_scala.file_read_write.server

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel._
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel

class BasicServer(host: String, port: Int, channelHandler: ChannelHandler) extends Runnable {

  def run() {
    val bossGroup: EventLoopGroup = new NioEventLoopGroup
    val workerGroup: EventLoopGroup = new NioEventLoopGroup
    try {
      val b: ServerBootstrap = new ServerBootstrap
      b.group(bossGroup, workerGroup)
       .channel(classOf[NioServerSocketChannel])
       .childHandler(channelHandler)
       .option[Integer](ChannelOption.SO_BACKLOG, 128)
       .childOption[java.lang.Boolean](ChannelOption.SO_KEEPALIVE, true)
      val f: ChannelFuture = b.bind(host, port).sync
      f.channel.closeFuture.sync
    }
    catch {
      case e: InterruptedException =>
        e.printStackTrace()
    } finally {
      workerGroup.shutdownGracefully
      bossGroup.shutdownGracefully
    }
  }
}