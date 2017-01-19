import java.nio.ByteBuffer
import java.nio.channels.AsynchronousChannelGroup
import java.nio.charset.StandardCharsets
import java.util.concurrent.ForkJoinPool

import scala.annotation.{switch, tailrec}
import scala.concurrent.ExecutionContext

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
      val path = arg.path
        .split("/")
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

  val MAX_REQUEST_LINE_AND_HEADERS = 8192

  object system {
    val threadPool = new ForkJoinPool(Runtime.getRuntime.availableProcessors())
    val asyncChannelGroup = AsynchronousChannelGroup.withThreadPool(threadPool)
    implicit val executionContext = ExecutionContext.fromExecutor(threadPool)
  }

  def genericHttpEntityFromBuffer[TopLine, Entity](
      parseTopLine: String => TopLine,
      createEntity: (TopLine, Map[String, String], ByteBuffer) => Entity,
      buffer: ByteBuffer,
      maxContentLength: Int): Option[Entity] = {

    val savedPosition = buffer.position()
    buffer.rewind()
    // Read until \n\n
    @tailrec
    def loop(acc: List[String],
             offset: Int,
             lastChar: Byte,
             i: Int): List[String] = {
      if (i == buffer.capacity()) Nil
      else {
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
      case rawTopLine :: rawHeaders =>
        val topLine = parseTopLine(rawTopLine)
        val headers = {
          val xs = rawHeaders map { rh =>
            val Array(name, value) = rh.split(":", 2)
            (name.trim.toLowerCase, value.trim)
          }
          xs.toMap
        }
        val body = headers.get(Header.ContentLength) match {
          case Some(lengthString) =>
            val length = lengthString.toInt
            if (length >= maxContentLength)
              throw new MaxContentLengthException()
            val savedLimit = buffer.limit()
            val bodyBuffer = ByteBuffer.allocate(length)
            buffer.limit(savedPosition)
            bodyBuffer.put(buffer.slice())
            buffer.limit(savedLimit)
            bodyBuffer
          case None =>
            ByteBuffer.allocate(0)
        }
        Some(createEntity(topLine, headers, body))
      case _ => None
    }
    buffer.position(savedPosition)
    result
  }
}
