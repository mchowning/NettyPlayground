package com.mattchowning_scala.file_read_write.client.calls

import com.mattchowning.file_read_write.SharedConstants
import com.mattchowning_scala.file_read_write.client.handler.ClientReadInboundFileHandler
import com.mattchowning.file_read_write.server.model.OAuthToken
import com.mattchowning_scala.file_read_write.client.FileReadWriteClient
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelOutboundInvoker
import io.netty.handler.codec.http.HttpClientCodec
import io.netty.handler.codec.http.HttpObjectAggregator

abstract class FileCall(var oAuthToken: OAuthToken,
                        val client: FileReadWriteClient,
                        successCallback: String => Unit,
                        failureCallback: () => Unit)
  extends Call[String](SharedConstants.FILE_HOST,
                       SharedConstants.FILE_PORT) {

  private val clientReadInboundFileHandler: ClientReadInboundFileHandler =
    new ClientReadInboundFileHandler(successCallback, failureCallback)
  override protected val getChannelHandlers: Array[ChannelHandler] =
    Array[ChannelHandler](new HttpClientCodec,
      new HttpObjectAggregator(MAX_BODY_LENGTH),
      clientReadInboundFileHandler)

  protected def makeAuthenticatedRequest(ctx: ChannelOutboundInvoker)

  override protected def makeRequest(ctx: ChannelOutboundInvoker) {
    if (oAuthToken.isExpired) {
      System.out.println("OAuth token expired.")
      client.refreshOAuthToken(refreshedOAuthToken => {
        this.oAuthToken = refreshedOAuthToken
        makeAuthenticatedRequest(ctx)
      }, () => {
        System.out.println("OAuth token refresh failed")
      })
    }
    else {
      makeAuthenticatedRequest(ctx)
    }
  }
}