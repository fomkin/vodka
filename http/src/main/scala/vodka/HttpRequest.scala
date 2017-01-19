package vodka

import java.nio.ByteBuffer
import java.nio.charset.{Charset, StandardCharsets}

/**
  * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
  */
case class HttpRequest(method: String,
                       path: String,
                       headers: Map[String, String],
                       body: ByteBuffer) {

  def toBuffer: ByteBuffer = {
    val headersString = {
      val xs = headers.map {
        case (k, v) => s"$k: $v"
      }
      xs.mkString("\r\n")
    }
    val requestListWithHeaders = {
      val s = headersString match {
        case "" => s"$method $path HTTP/1.1\r\n\r\n"
        case _ => s"$method $path HTTP/1.1\r\n$headersString\r\n\r\n"
      }
      s.getBytes(StandardCharsets.US_ASCII)
    }

    val buffer =
      ByteBuffer.allocate(body.capacity() + requestListWithHeaders.length)
    buffer.put(requestListWithHeaders)
    buffer.put(body)
    buffer.rewind()

    buffer
  }
}

object HttpRequest {

  def apply[T](method: String,
               path: String,
               body: Body[T],
               headers: Map[String, String],
               charset: Charset = StandardCharsets.UTF_8): HttpRequest = {

    val bodyBuffer = body.buffer(charset)
    HttpRequest(
        method = method,
        path = path,
        body = bodyBuffer,
        headers = {
          val headersWithContentLength =
            if (bodyBuffer.remaining > 0) headers + (Header.ContentLength -> bodyBuffer.remaining.toString)
            else headers
          if (headersWithContentLength.contains(Header.ContentType)) headers
          else headersWithContentLength + (Header.ContentType -> body.contentType(charset))
        }
    )
  }

  /**
    * Parse ByteBuffer to HttpRequest. Body is allocated
    * with Content-Length and contains data passed by buffer.
    *
    * @return None if header isn't complete and
    *         Some if header was read
    */
  def fromBuffer(buffer: ByteBuffer,
                 maxContentLength: Int): Option[HttpRequest] = {
    genericHttpEntityFromBuffer[RequestLine, HttpRequest](
        parseTopLine = { s =>
          val Array(method, path, version) = s.split(" ", 3)
          RequestLine(method, path, version)
        },
        createEntity = (topLine, headers, body) => {
          HttpRequest(topLine.method, topLine.path, headers, body)
        },
        buffer = buffer,
        maxContentLength = maxContentLength
    )
  }

  private case class RequestLine(method: String, path: String, version: String)
}
