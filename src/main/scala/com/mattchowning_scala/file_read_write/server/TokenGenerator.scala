package com.mattchowning_scala.file_read_write.server

import java.security.SecureRandom

object TokenGenerator {
  private val TOKEN_CHARS: String = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
  private val TOKEN_LENGTH: Int = 20
  private val RND: SecureRandom = new SecureRandom

  def generateNew: String = {
    val sb: StringBuilder = new StringBuilder(TOKEN_LENGTH)
    for (_ <- 0 until TOKEN_LENGTH) {
      val newChar = TOKEN_CHARS.charAt(RND.nextInt(TOKEN_CHARS.length()))
      sb.append(newChar)
    }
    sb.toString
  }
}