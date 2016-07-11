package vodka

import java.nio.ByteBuffer
import java.nio.charset.Charset

import scala.annotation.implicitNotFound

@implicitNotFound("Unable to convert value to response body. " +
                  "Define instance of ToResponseBody for this type " +
                  "to tell Vodka how to work with it.")
trait ToResponseBody[-T] {
  def toBuffer(value: T, charset: Charset): ByteBuffer
  def contentType: String
}

object ToResponseBody {

  implicit val string = new ToResponseBody[String] {
    def toBuffer(value: String, charset: Charset): ByteBuffer =
      ByteBuffer.wrap(value.getBytes(charset))
    def contentType: String = "text/plain"
  }

  implicit val byteBuffer = new ToResponseBody[ByteBuffer] {
    def toBuffer(value: ByteBuffer, charset: Charset): ByteBuffer = value
    val contentType = "application/octet-stream"
  }
}
