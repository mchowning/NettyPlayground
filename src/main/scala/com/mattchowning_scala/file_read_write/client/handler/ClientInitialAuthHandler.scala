package com.mattchowning_scala.file_read_write.client.handler

import com.google.gson.JsonSyntaxException
import com.mattchowning.file_read_write.server.model.OAuthToken
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.FullHttpResponse
import com.mattchowning.file_read_write.SharedConstants.GSON
import com.mattchowning.file_read_write.SharedConstants.RESPONSE_CHARSET

class ClientInitialAuthHandler extends SimpleChannelInboundHandler[FullHttpResponse] {
  private var oAuthToken: OAuthToken = null

  @throws(classOf[Exception])
  protected def channelRead0(ctx: ChannelHandlerContext, response: FullHttpResponse) {
    val responseBody: String = response.content.toString(RESPONSE_CHARSET)
    response.status.code match {
      case 200 =>
        val oAuthToken: OAuthToken = parseOAuthResponse(responseBody)
        if (oAuthToken != null) {
          this.oAuthToken = oAuthToken
          System.out.println("OAuth response received.")
        }
      case _ =>
        System.out.println("OAuth ERROR: " + responseBody)
    }
    ctx.close
  }

  private def parseOAuthResponse(responseBody: String): OAuthToken = {
    try {
      GSON.fromJson(responseBody, classOf[OAuthToken])
    } catch {
      case e: JsonSyntaxException =>
      e.printStackTrace()
      null
    }
  }

  @throws(classOf[Exception])
  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
    cause.printStackTrace()
    ctx.close
  }
}