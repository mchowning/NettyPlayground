package com.mattchowning_scala.file_read_write.client.handler

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.FullHttpResponse
import io.netty.util.CharsetUtil

class ClientReadInboundFileHandler extends SimpleChannelInboundHandler[FullHttpResponse] {

  private var fileContent: String = null

  @throws(classOf[Exception])
  protected def channelRead0(ctx: ChannelHandlerContext, response: FullHttpResponse) {
    val responseContent: String = getContent(response)
    if (response.status.code == 200) {
      fileContent = responseContent
    }
    else {
      System.out.println("Error retrieving file: " + responseContent)
    }
    ctx.close
  }

  private def getContent(msg: FullHttpResponse): String = {
    val msgBytes: Array[Byte] = new Array[Byte](msg.content.capacity)
    msg.content.getBytes(0, msgBytes)
    new String(msgBytes, CharsetUtil.UTF_8)
  }

  @throws(classOf[Exception])
  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
    cause.printStackTrace()
    ctx.close
  }
}