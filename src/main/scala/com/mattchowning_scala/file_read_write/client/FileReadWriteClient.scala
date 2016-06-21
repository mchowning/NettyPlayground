package com.mattchowning_scala.file_read_write.client

import com.mattchowning.file_read_write.server.model.OAuthToken
import com.mattchowning_scala.file_read_write.client.calls._
import org.jetbrains.annotations.NotNull

class FileReadWriteClient {

  private var oAuthToken: OAuthToken = null

  private val setOAuthToken: (OAuthToken => OAuthToken) = (token: OAuthToken) => {
    this.oAuthToken = token
    token
  }

  def setOAuthToken(token: OAuthToken): OAuthToken = {
    this.oAuthToken = token
    token
  }

  def retrieveFileContent(@NotNull consumer: String => Unit) {
    val call: Call[String] = new GetFileCall(oAuthToken, this)
    call.execute(consumer)
  }

  def updateFileContent(@NotNull newFileContent: String,
                        @NotNull consumer: String => Unit) {
    val call: Call[String] = new PostFileCall(oAuthToken, this, newFileContent)
    call.execute(consumer)
  }

  def retrieveOAuthToken(@NotNull externalConsumer: OAuthToken => Unit,
                         username: String,
                         password: String) {
    val call: Call[OAuthToken] = new GetOAuthCall(username, password)
    call.execute(setOAuthToken.andThen(externalConsumer))
  }

  def refreshOAuthToken(@NotNull externalConsumer: OAuthToken => Unit) {
    val call: Call[OAuthToken] = new RefreshOAuthCall(oAuthToken.getRefreshToken)
    call.execute(setOAuthToken.andThen(externalConsumer))
  }
}