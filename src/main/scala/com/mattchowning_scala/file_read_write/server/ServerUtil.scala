package com.mattchowning_scala.file_read_write.server

import com.mattchowning_scala.file_read_write.server.model.{HttpError, OAuthToken}
import com.mattchowning_scala.file_read_write.SharedConstants.GSON
import com.mattchowning_scala.file_read_write.SharedConstants.RESPONSE_CHARSET
import org.jetbrains.annotations.NotNull
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Base64
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http._
import io.netty.util.CharsetUtil

// TODO make into trait?
class ServerUtil {

  def getOAuthToken(encodedAuthorizationHeader: String): OAuthToken = {
    val tokenType: String = getTokenType(encodedAuthorizationHeader)
    val token: String = getAccessToken(encodedAuthorizationHeader)
    new OAuthToken(token, tokenType)
  }

  def getDate: String =
    DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now)

  def sendError(ctx: ChannelHandlerContext, status: HttpResponseStatus): Unit =
    sendError(ctx, status, status.toString)

  def sendError(ctx: ChannelHandlerContext,
                status: HttpResponseStatus,
                errorMsg: String): Unit =
    sendError(ctx, status, errorMsg, null)

  def sendError(ctx: ChannelHandlerContext,
                status: HttpResponseStatus,
                errorMsg: String,
                errorDescription: String): Unit = {
    val error: HttpError = new HttpError(errorMsg, errorDescription)
    val json: String = GSON.toJson(error)
    val response: FullHttpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                                                                 status,
                                                                 Unpooled.copiedBuffer(json,
                                                                                       RESPONSE_CHARSET))
    response.headers.set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8")
    HttpUtil.setContentLength(response, json.length)
    ctx.writeAndFlush(response)
  }

  private def getAccessToken(@NotNull authorizationHeader: String): String = {
    val authHeaderArray: Array[String] = authorizationHeader.split("\\s")
    if (authHeaderArray.length < 2) {
      ""
    } else {
      val encodedToken: String = authHeaderArray(1)
      val decodedTokenBytes: Array[Byte] = Base64.getDecoder.decode(encodedToken)
      new String(decodedTokenBytes, CharsetUtil.UTF_8)
    }
  }

  private def getTokenType(@NotNull encodedAuthorizationHeader: String): String =
    encodedAuthorizationHeader.split("\\s")(0)
}