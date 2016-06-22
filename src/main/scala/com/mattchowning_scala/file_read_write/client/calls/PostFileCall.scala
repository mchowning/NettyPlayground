package com.mattchowning_scala.file_read_write.client.calls

import com.mattchowning_scala.file_read_write.server.model.OAuthToken
import com.mattchowning_scala.file_read_write.client.FileReadWriteClient
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelOutboundInvoker
import io.netty.handler.codec.http._
import io.netty.util.CharsetUtil

class PostFileCall(_oAuthToken: OAuthToken,
                   _client: FileReadWriteClient,
                   successCallback: String => Unit,
                   failureCallback: () => Unit,
                   newFileContent: String)
  extends FileCall(_oAuthToken, _client, successCallback, failureCallback) {

  override def execute() {
    System.out.println("Requesting to post file content...")
    super.execute()
  }

  override protected def makeAuthenticatedRequest(ctx: ChannelOutboundInvoker) {
    val message: FullHttpMessage = new DefaultFullHttpRequest(
      HttpVersion.HTTP_1_1,
      HttpMethod.POST,
      "",
      Unpooled.copiedBuffer(newFileContent, CharsetUtil.UTF_8))
    message.headers.add(
      HttpHeaderNames.CONTENT_LENGTH,
      newFileContent.length).add(HttpHeaderNames.AUTHORIZATION,
      oAuthToken.getEncodedAuthorizationHeader)
    System.out.println("Posting updated file content...")
    ctx.writeAndFlush(message)
  }
}