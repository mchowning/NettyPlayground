package com.mattchowning_scala.file_read_write.server.handler

import com.mattchowning_scala.file_read_write.server.ServerUtil
import com.mattchowning_scala.file_read_write.server.model.OAuthToken
import com.mattchowning_scala.file_read_write.server.model.OAuthTokenMap
import io.netty.channel._
import io.netty.handler.codec.http._
import io.netty.util.ReferenceCountUtil

@ChannelHandler.Sharable
class ServerOAuthVerificationHandler(oAuthTokens: OAuthTokenMap, serverUtil: ServerUtil)
  extends SimpleChannelInboundHandler[FullHttpRequest] {

  def this(oAuthTokenMap: OAuthTokenMap) {
    this(oAuthTokenMap, new ServerUtil)
  }

  @throws(classOf[Exception])
  protected def channelRead0(ctx: ChannelHandlerContext, request: FullHttpRequest) {
    if (hasValidToken(ctx, request)) {
      forwardRequest(ctx, request)
    }
  }

  private def hasValidToken(ctx: ChannelHandlerContext, request: FullHttpRequest): Boolean = {
    var isAuthorized: Boolean = false
    if (request.headers.contains(HttpHeaderNames.AUTHORIZATION)) {
      val encodedAuthHeader: String = request.headers.get(HttpHeaderNames.AUTHORIZATION)
      val receivedOAuthToken: OAuthToken = serverUtil.getOAuthToken(encodedAuthHeader)
      if (!receivedOAuthToken.hasValidTokenType) {
        serverUtil.sendError(ctx, HttpResponseStatus.BAD_REQUEST, "invalid_grant", "Bearer token type required")
      } else if (!oAuthTokens.containsAccessToken(receivedOAuthToken)) {
        serverUtil.sendError(ctx, HttpResponseStatus.BAD_REQUEST, "invalid_grant", "Invalid token")
      } else if (oAuthTokens.getWithAccessToken(receivedOAuthToken.accessToken).isExpired) {
        serverUtil.sendError(ctx, HttpResponseStatus.BAD_REQUEST, "invalid_grant", "Token expired")
      } else {
        isAuthorized = true
      }
    } else {
      serverUtil.sendError(ctx, HttpResponseStatus.BAD_REQUEST, "invalid_request", "authorization header required")
    }
    isAuthorized
  }

  private def forwardRequest(ctx: ChannelHandlerContext, request: FullHttpRequest) {
    ReferenceCountUtil.retain(request)
    ctx.fireChannelRead(request)
  }

  @throws(classOf[Exception])
  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
    cause.printStackTrace()
    ctx.close
  }
}