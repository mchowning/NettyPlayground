package com.mattchowning_scala.file_read_write

import com.google.gson._
import java.nio.charset.Charset
import io.netty.util.CharsetUtil

// TODO turn into trait?
object SharedConstants {
  val OAUTH_PATH: String = "/oauth"
  val GRANT_TYPE_KEY: String = "grant_type"
  val GRANT_TYPE_PASSWORD: String = "password"
  val GRANT_TYPE_REFRESH_TOKEN: String = "refresh_token"
  val PASSWORD_KEY: String = "password"
  val USERNAME_KEY: String = "username"
  val REFRESH_TOKEN_KEY: String = "refresh_token"
  val RESPONSE_CHARSET: Charset = CharsetUtil.UTF_8
  val FILE_HOST: String = "localhost"
  val FILE_PORT: Int = 8081
  val OAUTH_HOST: String = FILE_HOST
  val OAUTH_PORT: Int = 8080
  val GSON: Gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create
}