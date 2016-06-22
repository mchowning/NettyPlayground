package com.mattchowning_scala.file_read_write.server.handler

import com.mattchowning_scala.file_read_write.server.ServerUtil
import com.mattchowning_scala.file_read_write.server.model.OAuthToken
import com.mattchowning_scala.file_read_write.server.model.OAuthTokenMap
import com.mattchowning_scala.file_read_write.SharedConstants._
import org.jetbrains.annotations.NotNull
import java.io.IOException
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http._
import io.netty.handler.codec.http.multipart._

class ServerOAuthRequestHandler(oAuthTokens: OAuthTokenMap, serverUtil: ServerUtil)
  extends SimpleChannelInboundHandler[FullHttpRequest] {

  def this(oAuthTokenMap: OAuthTokenMap) {
    this(oAuthTokenMap, new ServerUtil)
  }

  @throws(classOf[Exception])
  protected def channelRead0(ctx: ChannelHandlerContext, request: FullHttpRequest) {
    if (OAUTH_PATH == request.uri) {
      processOAuthRequest(ctx, request)
    }
    else {
      invalidEndpoint(ctx)
    }
  }

  @throws(classOf[IOException])
  private def processOAuthRequest(ctx: ChannelHandlerContext, request: FullHttpRequest) {
    if (request.method ne HttpMethod.POST) {
      serverUtil.sendError(ctx, HttpResponseStatus.BAD_REQUEST, "invalid_request", "oauth request must be POST")
    }
    else {
      val decoder: HttpPostRequestDecoder = new HttpPostRequestDecoder(request)
      val grantType: String = getValue(decoder, GRANT_TYPE_KEY)
      grantType match {
        case GRANT_TYPE_PASSWORD =>
          processPasswordRequest(ctx, decoder)
        case GRANT_TYPE_REFRESH_TOKEN =>
          processRefreshRequest(ctx, decoder)
        case _ =>
          val errorDescription: String = String.format("oauth request must specify grant_type of %s or %s", GRANT_TYPE_PASSWORD, GRANT_TYPE_REFRESH_TOKEN)
          serverUtil.sendError(ctx, HttpResponseStatus.BAD_REQUEST, "invalid_request", errorDescription)
      }
    }
  }

  @NotNull
  @throws(classOf[IOException])
  private def getValue(decoder: HttpPostRequestDecoder, key: String): String = {
    var result: String = null
    val bodyHttpDataForKey: InterfaceHttpData = decoder.getBodyHttpData(key)
    if (bodyHttpDataForKey != null && bodyHttpDataForKey.isInstanceOf[Attribute]) {
      // TODO find way to get rid of instanceOf checking
      result = bodyHttpDataForKey.asInstanceOf[Attribute].getValue
    }

    // TODO remove null handling
    if (result == null) "" else result
  }

  @throws(classOf[IOException])
  private def processPasswordRequest(ctx: ChannelHandlerContext, decoder: HttpPostRequestDecoder) {
    val username: String = getValue(decoder, USERNAME_KEY)
    val password: String = getValue(decoder, PASSWORD_KEY)
    if (username.isEmpty) {
      val errorDescription: String = "oauth request must specify username"
      serverUtil.sendError(ctx, HttpResponseStatus.BAD_REQUEST, "invalid_request", errorDescription)
    } else if (password.isEmpty) {
      val errorDescription: String = "oauth request must specify password"
      serverUtil.sendError(ctx, HttpResponseStatus.BAD_REQUEST, "invalid_request", errorDescription)
    } else if (isUserShady(username, password)) {
      serverUtil.sendError(ctx, HttpResponseStatus.BAD_REQUEST, "invalid_request", "user is shady")
    } else {
      respondWithNewOAuthToken(ctx)
    }
  }

  @throws(classOf[IOException])
  private def processRefreshRequest(ctx: ChannelHandlerContext, decoder: HttpPostRequestDecoder) {
    val refreshToken: String = getValue(decoder, REFRESH_TOKEN_KEY)
    if (oAuthTokens.containsRefreshToken(refreshToken)) {
      oAuthTokens.removeToken(oAuthTokens.getWithRefreshToken(refreshToken))
      respondWithNewOAuthToken(ctx)
    } else {
      val description: String = "valid refresh_token not provided"
      serverUtil.sendError(ctx, HttpResponseStatus.BAD_REQUEST, "invalid_request", description)
    }
  }

  private def respondWithNewOAuthToken(ctx: ChannelHandlerContext) {
    // FIXME fix error happening here
    val newToken: OAuthToken = new OAuthToken
    oAuthTokens.add(newToken)
    respondWithOAuthToken(ctx, newToken)
  }

  private def respondWithOAuthToken(ctx: ChannelHandlerContext, token: OAuthToken) {
    val body: String = GSON.toJson(token)
    val response: FullHttpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.copiedBuffer(body, RESPONSE_CHARSET))
    response.headers.set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=UTF-8")
    response.headers.set(HttpHeaderNames.DATE, serverUtil.getDate)
    response.headers.set(HttpHeaderNames.CONTENT_LENGTH, body.length)
    ctx.writeAndFlush(response)
  }

  private def invalidEndpoint(ctx: ChannelHandlerContext) {
    val response: FullHttpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND)
    ctx.writeAndFlush(response)
  }

  private def isUserShady(username: String, password: String): Boolean = {
    return "sleepynate" == username
  }
}