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

    // Status line
    stringBuilder
      .append("HTTP/1.1 ")
      .append(statusCode.value)
      .append(" ")
      .append(statusCode.reasonPhrase)
      .append("\r\n")

    // Headers
    for ((k, v) <- headers) {
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
  def Ok[T](body: ResponseBody[T],
            headers: Map[String, String] = Map.empty,
            charset: Charset = StandardCharsets.UTF_8): HttpResponse = {
    val bodyBuffer = body.buffer(charset)
    HttpResponse(
      statusCode =
        if (bodyBuffer.remaining > 0) StatusCode.`OK`
        else StatusCode.`No Content`,
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

  def create[T](statusCode: StatusCode,
                body: ResponseBody[T],
                headers: Map[String, String] = Map.empty,
                charset: Charset = StandardCharsets.UTF_8): HttpResponse = {
    val bodyBuffer = body.buffer(charset)
    HttpResponse(
        statusCode = statusCode,
        body = bodyBuffer,
        headers = {
          val headersWithContentLength = headers +
             (Header.ContentLength -> bodyBuffer.remaining.toString)
          if (headersWithContentLength.contains(Header.ContentType)) headers
          else headersWithContentLength + (Header.ContentType -> body.contentType(charset))
        }
    )
  }
}
