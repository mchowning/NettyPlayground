package com.mattchowning_scala.file_read_write.server.handler

import java.io._
import io.netty.buffer.ByteBuf

class FileManager(private val fullFilePath: String) {

  def getFile: File = {
    new File(fullFilePath)
  }

  def writeFileContent(byteBuf: ByteBuf): Boolean = {
    try {
      val bytes: Array[Byte] = getBytes(byteBuf)
      val fileStream: FileOutputStream = new FileOutputStream(getFile)
      fileStream.write(bytes)
      true
    } catch {
      case e: IOException =>
        e.printStackTrace()
        false
    }
  }

  private def getBytes(buf: ByteBuf): Array[Byte] = {
    val result: Array[Byte] = new Array[Byte](buf.readableBytes)
    buf.getBytes(buf.readerIndex, result)
    result
  }
}