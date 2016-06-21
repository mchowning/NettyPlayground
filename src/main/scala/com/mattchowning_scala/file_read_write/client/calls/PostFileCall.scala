package com.mattchowning_scala.file_read_write.client.calls

import com.mattchowning.file_read_write.server.model.OAuthToken
import com.mattchowning_scala.file_read_write.client.FileReadWriteClient
import org.jetbrains.annotations.NotNull
import java.util.function.Consumer
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelOutboundInvoker
import io.netty.handler.codec.http._
import io.netty.util.CharsetUtil

class PostFileCall(@NotNull _oAuthToken: OAuthToken,
                   @NotNull _client: FileReadWriteClient,
                   @NotNull newFileContent: String)
  extends FileCall(_oAuthToken, _client) {

  override def execute(@NotNull resultConsumer: String => Unit) {
    System.out.println("Requesting to post file content...")
    super.execute(resultConsumer)
  }

  override protected def makeAuthenticatedRequest(ctx: ChannelOutboundInvoker) {
    val message: FullHttpMessage = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "", Unpooled.copiedBuffer(newFileContent, CharsetUtil.UTF_8))
    message.headers.add(HttpHeaderNames.CONTENT_LENGTH, newFileContent.length).add(HttpHeaderNames.AUTHORIZATION, oAuthToken.getEncodedAuthorizationHeader)
    System.out.println("Posting updated file content...")
    ctx.writeAndFlush(message)
  }
}