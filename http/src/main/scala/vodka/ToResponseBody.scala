package vodka

import java.nio.ByteBuffer
import java.nio.charset.Charset

trait ToResponseBody[T] {
  def toBuffer(value: T, charset: Charset): ByteBuffer
  def contentType: String
}
