package com.mattchowning_scala.file_read_write.client.calls

import com.mattchowning.file_read_write.server.model.OAuthToken
import com.mattchowning_scala.file_read_write.client.FileReadWriteClient
import org.jetbrains.annotations.NotNull
import java.util.function.Consumer
import io.netty.channel.ChannelOutboundInvoker
import io.netty.handler.codec.http._

class GetFileCall(@NotNull _oAuthToken: OAuthToken,
                  @NotNull _client: FileReadWriteClient)
  extends FileCall(_oAuthToken, _client) {

  override def execute(@NotNull resultConsumer: String => Unit) {
    System.out.println("Requesting file content...")
    super.execute(resultConsumer)
  }

  override protected def makeAuthenticatedRequest(ctx: ChannelOutboundInvoker) {
    val message: FullHttpMessage = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "")
    message.headers.add(HttpHeaderNames.AUTHORIZATION, oAuthToken.getEncodedAuthorizationHeader)
    ctx.writeAndFlush(message)
  }
}