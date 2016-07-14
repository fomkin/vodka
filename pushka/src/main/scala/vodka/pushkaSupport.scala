package vodka

import java.nio.ByteBuffer
import java.nio.charset.{Charset, StandardCharsets}

import pushka.Ast

object pushkaSupport {

  implicit final class StringResponseBody(val ast: Ast) extends AnyVal with ResponseBody[Ast] {
    def buffer(charset: Charset): ByteBuffer =
      ByteBuffer.wrap(pushka.json.printer.print(ast).getBytes(charset))
    def contentType(charset: Charset): String =
      s"application/json; charset=${charset.displayName}"
  }

  object fromJson {
    val JsonContentType = "application/json"
    val CharSetPattern = """(.+);.*charset=(.+)""".r
    def unapply(request: HttpRequest): Option[Ast] = {
      val s = request.headers.get(Header.ContentType) match {
        case Some(CharSetPattern(JsonContentType, charset)) =>
          try {
            new String(request.body.array(), charset)
          } catch {
            case _: java.io.UnsupportedEncodingException =>
              throw new vodka.UnsupportedCharsetException(charset)
          }
        case Some(JsonContentType) =>
          new String(request.body.array(), StandardCharsets.UTF_8)
        case Some(contentType) =>
          throw new UnsupportedMediaType(
            expected = JsonContentType,
            given = contentType
          )
        case None =>
          new String(request.body.array(), StandardCharsets.UTF_8)
      }
      Some(pushka.json.parser.parse(s))
    }
  }

}
