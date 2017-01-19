package vodka

import java.nio.ByteBuffer
import java.nio.charset.Charset

trait Body[+T] extends Any {
  def buffer(charset: Charset): ByteBuffer
  def contentType(charset: Charset): String
}

object Body {

  implicit final class StringBody(val s: String) extends AnyVal with Body[String] {
    def buffer(charset: Charset): ByteBuffer =
      ByteBuffer.wrap(s.getBytes(charset))
    def contentType(charset: Charset): String =
      s"text/plain; charset=${charset.displayName}"
  }

  implicit final class ByteBufferBody(val bb: ByteBuffer) extends AnyVal with Body[ByteBuffer] {
    def buffer(charset: Charset): ByteBuffer = bb
    def contentType(charset: Charset) = "application/octet-stream"
  }
}
