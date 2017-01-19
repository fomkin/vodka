package vodka

import java.nio.ByteBuffer
import java.nio.charset.{Charset, StandardCharsets}

import pushka.Ast

object pushkaSupport {

  implicit final class StringBody(val ast: Ast) extends AnyVal with Body[Ast] {
    def buffer(charset: Charset): ByteBuffer =
      ByteBuffer.wrap(pushka.json.printer.print(ast).getBytes(charset))
    def contentType(charset: Charset): String =
      s"application/json; charset=${charset.displayName}"
  }

  object fromJson {
    def unapply(request: HttpRequest): Option[Ast] = {
      val s = request.headers.get(Header.ContentType) match {
        case Some(Header.CharsetPattern(Header.JsonContentType, charset)) =>
          try {
            new String(request.body.array(), charset)
          } catch {
            case _: java.io.UnsupportedEncodingException =>
              throw new vodka.UnsupportedCharsetException(charset)
          }
        case Some(Header.JsonContentType) =>
          new String(request.body.array(), StandardCharsets.UTF_8)
        case Some(contentType) =>
          throw new UnsupportedMediaType(
            expected = Header.JsonContentType,
            given = contentType
          )
        case None =>
          new String(request.body.array(), StandardCharsets.UTF_8)
      }
      Some(pushka.json.parser.parse(s))
    }
  }

}
