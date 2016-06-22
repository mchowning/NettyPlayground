package com.mattchowning_scala.file_read_write.server.model

import com.mattchowning_scala.file_read_write.server.TokenGenerator
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.Base64
import io.netty.util.CharsetUtil

object OAuthToken {
  val TOKEN_DURATION_IN_SECONDS: Long = 7
  private val PROPER_TOKEN_TYPE: String = "Bearer"
}

class OAuthToken(val accessToken: String, val refreshToken: String) {

  def this() {
    this(TokenGenerator.generateNew, TokenGenerator.generateNew)
  }

  private final val tokenType: String = OAuthToken.PROPER_TOKEN_TYPE
  private final val expiresIn: Long = OAuthToken.TOKEN_DURATION_IN_SECONDS
  private final val generatedTime: Long = currentTime

  def isExpired: Boolean = currentTime - generatedTime > OAuthToken.TOKEN_DURATION_IN_SECONDS

  def hasValidTokenType: Boolean = OAuthToken.PROPER_TOKEN_TYPE == tokenType

  def getEncodedAuthorizationHeader: String = {
    val encodedTokenBytes: Array[Byte] = Base64.getEncoder.encode(accessToken.getBytes)
    String.format("%s %s", tokenType, new String(encodedTokenBytes, CharsetUtil.UTF_8))
  }

  private def currentTime: Long = ZonedDateTime.now(ZoneOffset.UTC).toEpochSecond
}