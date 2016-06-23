import java.nio.ByteBuffer
import java.nio.charset.Charset

package object vodka {

  sealed trait Path
  case class /(prev: Path, value: String) extends Path
  case object Root extends Path

  /**
    * Use it to match request once more
    * @example
    * ```scala
    * object Json {
    *   def unapply(request: Request): Option[JsonAst] = ???
    * }
    *
    * Vodka {
    *   case Json(json) <| GET -> Root / "hello" / user =>
    *     // ...
    * }
    * ```
    */
  object <| {
    def unapply(request: HttpRequest): Option[(HttpRequest, HttpRequest)] = {
      Some(request, request)
    }
  }

  object -> {
    def unapply(arg: HttpRequest): Option[(String, Path)] = {
      val path = arg.path.split("/")
        .toList
        .filter(_.nonEmpty)
        .foldLeft(Root: Path)((xs, x) => /(xs, x))
      val tpl = (arg.method, path)
      Some(tpl)
    }
  }

  val GET = "GET"
  val POST = "POST"
  val PUT = "PUT"
  val DELETE = "DELETE"

  implicit val stringToResponseBody = new ToResponseBody[String] {
    def toBuffer(value: String, charset: Charset): ByteBuffer =
      ByteBuffer.wrap(value.getBytes(charset))
    def contentType: String = "text/plain"
  }
}
