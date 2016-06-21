package com.mattchowning_scala.file_read_write.client.calls

import io.netty.channel.ChannelOutboundInvoker
import io.netty.handler.codec.http._
import io.netty.handler.codec.http.multipart.HttpPostRequestEncoder
import com.mattchowning.file_read_write.SharedConstants._

class RefreshOAuthCall(refreshToken: String) extends OAuthCall {

  protected def makeRequest(ctx: ChannelOutboundInvoker) {
    System.out.println("Attempting to refresh OAuth token...")
    try {
      val request: FullHttpRequest = getRefreshTokenRequest
      ctx.writeAndFlush(request)
    }
    catch {
      case e: HttpPostRequestEncoder.ErrorDataEncoderException =>
        e.printStackTrace()
        ctx.close
    }
  }

  @throws(classOf[HttpPostRequestEncoder.ErrorDataEncoderException])
  private def getRefreshTokenRequest: FullHttpRequest = {
    val request: FullHttpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, OAUTH_PATH)
    val postRequestEncoder: HttpPostRequestEncoder = new HttpPostRequestEncoder(request, false)
    postRequestEncoder.addBodyAttribute(GRANT_TYPE_KEY, GRANT_TYPE_REFRESH_TOKEN)
    postRequestEncoder.addBodyAttribute(REFRESH_TOKEN_KEY, refreshToken)
    postRequestEncoder.finalizeRequest
    request
  }
}