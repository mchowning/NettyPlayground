package com.mattchowning_scala.file_read_write.server.model

class OAuthTokenMap {

  private var issuedTokens: Set[OAuthToken] = Set()

  def add(oAuthToken: OAuthToken): Boolean = {
    val alreadyContainsToken: Boolean = issuedTokens.contains(oAuthToken)
    if (alreadyContainsToken) {
      System.err.println("Error: token already added to TokenMap")
      false
    } else {
      issuedTokens += oAuthToken
      true
    }
  }

  def containsAccessToken(oAuthToken: OAuthToken): Boolean =
  issuedTokens.exists(issuedToken => issuedToken.accessToken == oAuthToken.accessToken)

  def containsRefreshToken(refreshToken: String): Boolean =
    issuedTokens.exists(issuedToken => issuedToken.refreshToken == refreshToken)

  // TODO return option (i.e., drop the get, and drop the contains method)
  def getWithAccessToken(accessToken: String): OAuthToken =
    issuedTokens.find(issuedToken => issuedToken.accessToken == accessToken).get

  // TODO return option?
  def getWithRefreshToken(refreshToken: String): OAuthToken =
    issuedTokens.find(issuedToken => issuedToken.refreshToken == refreshToken).get

  def removeToken(token: OAuthToken): Boolean = {
    val containedToken = issuedTokens.contains(token)
    if (containedToken) issuedTokens -= token
    containedToken
  }
}