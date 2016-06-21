package com.mattchowning_scala.file_read_write.client.calls

import com.mattchowning.file_read_write.SharedConstants
import com.mattchowning.file_read_write.client.handler.ClientInitialAuthHandler
import com.mattchowning.file_read_write.server.model.OAuthToken
import java.util.function.Supplier
import io.netty.channel.ChannelHandler
import io.netty.handler.codec.http.HttpClientCodec
import io.netty.handler.codec.http.HttpObjectAggregator

abstract class OAuthCall extends Call[OAuthToken](SharedConstants.OAUTH_HOST, SharedConstants.OAUTH_PORT) {

  private val initialAuthHandler: ClientInitialAuthHandler = new ClientInitialAuthHandler
  override protected val getResults: () => OAuthToken = initialAuthHandler.getOAuthToken
  override protected val getChannelHandlers: Array[ChannelHandler] =
    Array[ChannelHandler](new HttpClientCodec,
      new HttpObjectAggregator(MAX_BODY_LENGTH),
      initialAuthHandler)
}