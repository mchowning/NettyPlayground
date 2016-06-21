package com.mattchowning_scala.file_read_write.client.handler

import com.google.gson.JsonSyntaxException
import com.mattchowning.file_read_write.server.model.OAuthToken
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.FullHttpResponse
import com.mattchowning.file_read_write.SharedConstants.GSON
import com.mattchowning.file_read_write.SharedConstants.RESPONSE_CHARSET

class ClientInitialAuthHandler(successHandler: OAuthToken => Unit,
                               failureHandler: () => Unit)
  extends SimpleChannelInboundHandler[FullHttpResponse] {

  @throws(classOf[Exception])
  protected def channelRead0(ctx: ChannelHandlerContext, response: FullHttpResponse) {
    val responseBody: String = response.content.toString(RESPONSE_CHARSET)
    response.status.code match {
      case 200 =>
        val oAuthToken: OAuthToken = parseOAuthResponse(responseBody)
        if (oAuthToken != null) {
          System.out.println("OAuth response received.")
          successHandler(oAuthToken)
        }
      case _ =>
        System.out.println("OAuth ERROR: " + responseBody)
        failureHandler()
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