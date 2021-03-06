package vodka

import java.nio.ByteBuffer
import java.nio.charset.Charset

trait ResponseBody[+T] extends Any {
  def buffer(charset: Charset): ByteBuffer
  def contentType(charset: Charset): String
}

object ResponseBody {

  implicit final class StringResponseBody(val s: String) extends AnyVal with ResponseBody[String] {
    def buffer(charset: Charset): ByteBuffer =
      ByteBuffer.wrap(s.getBytes(charset))
    def contentType(charset: Charset): String =
      s"text/plain; charset=${charset.displayName}"
  }

  implicit final class ByteBufferResponseBody(val bb: ByteBuffer) extends AnyVal with ResponseBody[ByteBuffer] {
    def buffer(charset: Charset): ByteBuffer = bb
    def contentType(charset: Charset) = "application/octet-stream"
  }
}
