package com.mattchowning_scala.file_read_write.server

import com.mattchowning_scala.file_read_write.server.handler._
import com.mattchowning_scala.file_read_write.SharedConstants._
import com.mattchowning_scala.file_read_write.server.model.OAuthTokenMap
import io.netty.channel._
import io.netty.handler.codec.http.HttpObjectAggregator
import io.netty.handler.codec.http.HttpServerCodec
import io.netty.util.internal.SystemPropertyUtil
import java.io.File

object FileReadWriteServer {
  private val MAX_BODY_LENGTH: Int = 15000
  private val RELATIVE_FILE_PATH: String = "src/main/java/com/mattchowning/file_read_write/server/SecretServerFile.txt"
  private val FULL_FILE_PATH: String = SystemPropertyUtil.get("user.dir") + File.separator + RELATIVE_FILE_PATH

  @throws(classOf[Exception])
  def main(args: Array[String]) {
    val tokenMap = new OAuthTokenMap
    startOAuthServer(tokenMap)
    startFileServer(tokenMap)
  }

  private def startOAuthServer(tokenMap: OAuthTokenMap) {
    val oAuthChannelInitializer: ChannelInitializer[Channel] = new ChannelInitializer[Channel]() {
      @throws(classOf[Exception])
      protected def initChannel(ch: Channel) {
        ch.pipeline.addLast(new HttpServerCodec,
                            new HttpObjectAggregator(MAX_BODY_LENGTH),
                            new ServerOAuthRequestHandler(tokenMap))
      }
    }
    startServer(OAUTH_HOST, OAUTH_PORT, oAuthChannelInitializer)
  }

  private def startFileServer(tokenMap: OAuthTokenMap) {
    val fileChannelInitializer: ChannelInitializer[Channel] = new ChannelInitializer[Channel]() {
      @throws(classOf[Exception])
      protected def initChannel(ch: Channel) {
        ch.pipeline.addLast(new HttpServerCodec,
                            new HttpObjectAggregator(MAX_BODY_LENGTH),
                            new ServerOAuthVerificationHandler(tokenMap),
                            new ServerFileReadWriteHandler(FULL_FILE_PATH))
      }
    }
    startServer(FILE_HOST, FILE_PORT, fileChannelInitializer)
  }

  private def startServer(host: String, port: Int, channelHandler: ChannelHandler) {
    val server: BasicServer = new BasicServer(host, port, channelHandler)
    new Thread(server).start()
  }
}