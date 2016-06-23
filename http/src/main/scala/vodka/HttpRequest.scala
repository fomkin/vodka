package vodka

import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets

import scala.annotation.{switch, tailrec}

/**
  * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
  */
case class HttpRequest(method: String, path: String, headers: Map[String, String], body: ByteBuffer)

object HttpRequest {

  /**
    * Parse ByteBuffer to HttpRequest. Body is allocated
    * with Content-Length and contains data passed by buffer.
    * @return None if header isn't complete and
    *         Some if header was read
    */
  def fromBuffer(buffer: ByteBuffer): Option[HttpRequest] = {
    val savedPosition = buffer.position()
    buffer.rewind()
    // Read until \n\n
    @tailrec def loop(acc: List[String], offset: Int, lastChar: Byte, i: Int): List[String] = {
      if (i == buffer.capacity()) Nil else {
        (buffer.get(i): @switch) match {
          case '\n' if lastChar == '\n' =>
            buffer.position(i + 1)
            acc.reverse
          case '\r' => loop(acc, offset, lastChar, i + 1)
          case '\n' =>
            val stringBuffer = new Array[Byte](i - offset)
            buffer.position(offset)
            buffer.get(stringBuffer)
            val string = new String(stringBuffer, StandardCharsets.ISO_8859_1)
            loop(string.trim :: acc, i, '\n', i + 1)
          case char =>
            loop(acc, offset, char, i + 1)
        }
      }
    }
    val result = loop(Nil, 0, '\r', 0) match {
      case Nil => None
      case RequestLine(Array(method, path, _)) :: rawHeaders =>
        val headers = {
          val xs = rawHeaders map { rh =>
            val Array(name, value) = rh.split(":", 2)
            (name.trim.toLowerCase, value.trim)
          }
          xs.toMap
        }
        val body = headers.get(Header.ContentLength) match {
          case Some(length) =>
            val savedLimit = buffer.limit()
            val bodyBuffer = ByteBuffer.allocate(length.toInt)
            buffer.limit(savedPosition)
            bodyBuffer.put(buffer.slice())
            buffer.limit(savedLimit)
            bodyBuffer
          case None =>
            ByteBuffer.allocate(0)
        }
        Some(HttpRequest(method, path, headers, body))
      case _ => None
    }
    buffer.position(savedPosition)
    result
  }

  private object RequestLine {
    def unapply(arg: String): Option[Array[String]] =
      Some(arg.split(" ", 3))
  }
}
