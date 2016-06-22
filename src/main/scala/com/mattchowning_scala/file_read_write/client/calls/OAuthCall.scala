package com.mattchowning_scala.file_read_write.client.calls

import com.mattchowning_scala.file_read_write.SharedConstants
import com.mattchowning_scala.file_read_write.client.handler.ClientInitialAuthHandler
import com.mattchowning_scala.file_read_write.server.model.OAuthToken
import io.netty.channel.ChannelHandler
import io.netty.handler.codec.http.HttpClientCodec
import io.netty.handler.codec.http.HttpObjectAggregator

abstract class OAuthCall(successCallback: OAuthToken => Unit,
                         failureCallback: () => Unit)
  extends Call[OAuthToken](SharedConstants.OAUTH_HOST,
                           SharedConstants.OAUTH_PORT) {

  private val initialAuthHandler: ClientInitialAuthHandler =
    new ClientInitialAuthHandler(successCallback, failureCallback)
  override protected val getChannelHandlers: Array[ChannelHandler] =
    Array[ChannelHandler](new HttpClientCodec,
      new HttpObjectAggregator(MAX_BODY_LENGTH),
      initialAuthHandler)
}