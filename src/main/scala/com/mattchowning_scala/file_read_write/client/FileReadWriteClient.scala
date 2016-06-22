package com.mattchowning_scala.file_read_write.client

import com.mattchowning_scala.file_read_write.server.model.OAuthToken
import com.mattchowning_scala.file_read_write.client.calls._

class FileReadWriteClient {

  private var oAuthToken: OAuthToken = null

  private val setOAuthToken: (OAuthToken => OAuthToken) = (token: OAuthToken) => {
    this.oAuthToken = token
    token
  }

  def retrieveFileContent(successCallback: String => Unit, failureCallback: () => Unit) {
    val call: Call[String] = new GetFileCall(oAuthToken, this, successCallback, failureCallback)
    call.execute()
  }

  def updateFileContent(newFileContent: String,
                        successCallback: String => Unit,
                        failureCallback: () => Unit) {
    val call: Call[String] = new PostFileCall(oAuthToken, this, successCallback, failureCallback, newFileContent)
    call.execute()
  }

  def retrieveOAuthToken(successCallback: OAuthToken => Unit,
                         failureCallback: () => Unit,
                         username: String,
                         password: String) {
    val call: Call[OAuthToken] = new GetOAuthCall(username,
                                                  password,
                                                  setOAuthToken.andThen(successCallback),
                                                  failureCallback)
    call.execute()
  }

  def refreshOAuthToken(successCallback: OAuthToken => Unit, failureCallback: () => Unit) {
    val call: Call[OAuthToken] = new RefreshOAuthCall(oAuthToken.refreshToken,
                                                      setOAuthToken.andThen(successCallback),
                                                      failureCallback)
    call.execute()
  }
}