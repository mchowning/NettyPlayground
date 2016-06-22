package com.mattchowning_scala.file_read_write.server.handler

import com.mattchowning_scala.file_read_write.server.ServerUtil
import java.io.File
import javax.activation.MimetypesFileTypeMap
import io.netty.buffer.ByteBuf
import io.netty.channel._
import io.netty.handler.codec.http._

class ServerFileReadWriteHandler(fileManager: FileManager, serverUtil: ServerUtil)
  extends SimpleChannelInboundHandler[FullHttpRequest] {

  def this(fullFilePath: String) {
    this(new FileManager(fullFilePath), new ServerUtil)
  }

  @throws(classOf[Exception])
  protected def channelRead0(ctx: ChannelHandlerContext, request: FullHttpRequest) {
    request.method.toString match {
      case "GET"  => returnFileContent(ctx)
      case "POST" => writeFileContent(ctx, request.content)
      case _      => serverUtil.sendError(ctx, HttpResponseStatus.METHOD_NOT_ALLOWED)
    }
  }

  private def returnFileContent(ctx: ChannelHandlerContext) {
    val file: File = fileManager.getFile
    val response: HttpResponse = getHttpResponse(file)
    val fileContent: DefaultFileRegion = new DefaultFileRegion(file, 0, file.length)
    ctx.write(response)
    ctx.writeAndFlush(fileContent)
  }

  private def getHttpResponse(file: File): HttpResponse = {
    val response: HttpResponse = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK)
    response.headers.set(HttpHeaderNames.CONTENT_LENGTH, file.length)
    response.headers.set(HttpHeaderNames.CONTENT_TYPE, getContentType(file))
    response.headers.set(HttpHeaderNames.DATE, serverUtil.getDate)
    response
  }

  private def writeFileContent(ctx: ChannelHandlerContext, content: ByteBuf) {
    val succeeded: Boolean = fileManager.writeFileContent(content)
    if (succeeded) {
      returnFileContent(ctx)
    } else {
      serverUtil.sendError(ctx, HttpResponseStatus.NO_CONTENT)
    }
  }

  private def getContentType(file: File): String = {
    new MimetypesFileTypeMap().getContentType(file.getPath)
  }

  @throws(classOf[Exception])
  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
    cause.printStackTrace()
    ctx.close
  }
}