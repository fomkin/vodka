package vodka

import java.nio.ByteBuffer
import java.nio.charset.{Charset, StandardCharsets}

/**
  * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
  */
case class HttpResponse(
    statusCode: StatusCode, headers: Map[String, String], body: ByteBuffer) {

  def toBuffer: ByteBuffer = {

    body.rewind()

    val remaining = body.remaining()
    val stringBuilder = StringBuilder.newBuilder

    val headersWithContentLength = {
      if (remaining == 0) headers
      else headers + (Header.ContentLength -> remaining.toString)
    }

    // Status line
    stringBuilder
      .append("HTTP/1.1 ")
      .append(statusCode.value)
      .append(" ")
      .append(statusCode.reasonPhrase)
      .append("\r\n")

    // Headers
    for ((k, v) <- headersWithContentLength) {
      stringBuilder.append(k).append(": ").append(v).append("\r\n")
    }
    stringBuilder.append("\r\n")

    val headerBytes =
      stringBuilder.mkString.getBytes(StandardCharsets.ISO_8859_1)
    val writeBuffer = ByteBuffer.allocate(headerBytes.length + remaining)

    writeBuffer.put(headerBytes).put(body).rewind()

    writeBuffer
  }
}

object HttpResponse {
  def Ok[T](body: T,
            headers: Map[String, String] = Map.empty,
            charset: Charset = StandardCharsets.UTF_8)(
      implicit ev: ToResponseBody[T]): HttpResponse = {
    create(StatusCode.`OK`, body, headers, charset)
  }

  def create[T](statusCode: StatusCode,
                body: T,
                headers: Map[String, String] = Map.empty,
                charset: Charset = StandardCharsets.UTF_8)(
      implicit ev: ToResponseBody[T]): HttpResponse = {
    val bodyBuffer = ev.toBuffer(body, charset)
    HttpResponse(
        statusCode = statusCode,
        body = bodyBuffer,
        headers =
          if (headers.contains(Header.ContentType)) headers
          else headers + (Header.ContentType -> ev.contentType)
    )
  }
}
