package vodka

import java.nio.ByteBuffer
import java.nio.charset.{Charset, StandardCharsets}

/**
  * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
  */
case class HttpResponse(
    statusCode: StatusCode, headers: Map[String, String], body: ByteBuffer) {

  def stringBody(): String = {
    try {
      headers.get(Header.ContentType) match {
        case Some(Header.CharsetPattern(_, charset)) => new String(body.array(), charset)
        case None => new String(body.array(), StandardCharsets.UTF_8)
      }
    } catch {
      case e: MatchError =>
        new String(body.array(), StandardCharsets.UTF_8)
    }
  }

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
  def Ok[T](body: Body[T],
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
                body: Body[T],
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

  def fromBuffer(buffer: ByteBuffer,
                 maxContentLength: Int): Option[HttpResponse] = {
    genericHttpEntityFromBuffer[ResponseLine, HttpResponse](
      parseTopLine = { s =>
        val Array(version, statusCode, reasonPhrase) = s.split(" ", 3)
        ResponseLine(version, statusCode.toInt, reasonPhrase)
      },
      createEntity = (topLine, headers, body) => {
        HttpResponse(StatusCode(topLine.statusCode), headers, body)
      },
      buffer = buffer,
      maxContentLength = maxContentLength
    )
  }

  private case class ResponseLine(version: String, statusCode: Int, reasonPhrase: String)
}
