package com.mattchowning_scala.file_read_write.client.calls

import com.mattchowning.file_read_write.server.model.OAuthToken
import io.netty.channel.ChannelOutboundInvoker
import io.netty.handler.codec.http._
import io.netty.handler.codec.http.multipart.HttpPostRequestEncoder
import com.mattchowning.file_read_write.SharedConstants._

class GetOAuthCall(username: String,
                   password: String,
                   successCallback: OAuthToken => Unit,
                   failureCallback: () => Unit)
  extends OAuthCall(successCallback, failureCallback) {

  protected def makeRequest(ctx: ChannelOutboundInvoker) {
    System.out.println("Requesting OAuth token...")
    try {
      val request: FullHttpRequest = getOAuthTokenRequest
      ctx.writeAndFlush(request)
    }
    catch {
      case e: HttpPostRequestEncoder.ErrorDataEncoderException =>
        e.printStackTrace()
        ctx.close
    }
  }

  @throws(classOf[HttpPostRequestEncoder.ErrorDataEncoderException])
  private def getOAuthTokenRequest: FullHttpRequest = {
    val request: FullHttpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, OAUTH_PATH)
    val postRequestEncoder: HttpPostRequestEncoder = new HttpPostRequestEncoder(request, false)
    postRequestEncoder.addBodyAttribute(GRANT_TYPE_KEY, GRANT_TYPE_PASSWORD)
    postRequestEncoder.addBodyAttribute(USERNAME_KEY, username)
    postRequestEncoder.addBodyAttribute(PASSWORD_KEY, password)
    postRequestEncoder.finalizeRequest
    request
  }
}