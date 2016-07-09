package vodka

import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels._
import java.util.concurrent.ForkJoinPool

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

object Vodka {

  val MAX_REQUEST_LINE_AND_HEADERS = 8192
  private val threadPool = new ForkJoinPool(Runtime.getRuntime.availableProcessors())
  implicit val executionContext = ExecutionContext.fromExecutor(threadPool)

  type RequestHandler = PartialFunction[HttpRequest, Future[HttpResponse]]
  type NotFoundHandler = HttpRequest => Future[HttpResponse]
  type ErrorHandler = (Throwable) => HttpResponse
  type LogError = (String, Option[Throwable]) => Unit

  def apply(host: String = "0.0.0.0",
            port: Int = 9090,
            logError: LogError = logError,
            errorHandler: ErrorHandler = errorHandler,
            notFoundHandler: NotFoundHandler = notFoundHandler)(
      onRequest: RequestHandler): AsynchronousServerSocketChannel = {

    val serverAddress = new InetSocketAddress(host, port)

    def cb[T](f: CompletionHandler[T, Unit] => Unit)(
        cb: Try[T] => Unit): Unit = {
      try {
        f(new CompletionHandler[T, Unit] {
          def completed(result: T, attachment: Unit): Unit =
            cb(Success(result))
          def failed(exc: Throwable, attachment: Unit): Unit = cb(Failure(exc))
        })
      } catch {
        case exc: Throwable =>
          cb(Failure(exc))
      }
    }

    def readBody(clientChannel: AsynchronousSocketChannel,
                 request: HttpRequest): Future[Unit] = {
      val promise = Promise[Unit]()
      val read = cb[Integer](clientChannel.read(request.body, (), _)) _
      def loop(): Unit = read {
        case Success(length) =>
          if (length > 0) loop()
          else promise.success(())
        case Failure(exc) =>
          promise.failure(exc)
      }
      loop()
      promise.future
    }

    def readHeader(
        headerBuffer: ByteBuffer,
        clientChannel: AsynchronousSocketChannel): Future[HttpRequest] = {
      val promise = Promise[HttpRequest]()

      def loop(): Unit = cb[Integer](clientChannel.read(headerBuffer, (), _)) {
        case Success(length) =>
          if (length > 0) {
            HttpRequest.fromBuffer(headerBuffer) match {
              case None => loop()
              case Some(request) =>
                promise.success(request)
            }
          }
        case Failure(exc) =>
          promise.failure(exc)
      }

      loop()
      promise.future
    }

    val asyncChannelGroup = AsynchronousChannelGroup.withThreadPool(threadPool)
    val serverChannel = AsynchronousServerSocketChannel
      .open(asyncChannelGroup)
      .bind(serverAddress)

    val accept = cb[AsynchronousSocketChannel](serverChannel.accept((), _)) _

    def acceptLoop(): Unit = {
      accept {
        case Success(clientChannel) =>
          def writeAndClose(buffer: ByteBuffer): Unit = {
            buffer.rewind()
            cb[Integer](clientChannel.write(buffer, (), _)) {
              case Success(_) =>
                if (buffer.hasRemaining) writeAndClose(buffer)
                else clientChannel.close()
              case Failure(exc) =>
                clientChannel.close()
                logError("Error occurred while writing to socket", Some(exc))
            }
          }
          val headerBuffer = ByteBuffer.allocate(MAX_REQUEST_LINE_AND_HEADERS)
          val responseFuture =
            readHeader(headerBuffer, clientChannel) flatMap { request =>
              readBody(clientChannel, request) flatMap { _ =>
                if (onRequest.isDefinedAt(request)) onRequest(request)
                else notFoundHandler(request)
              }
            }
          responseFuture onComplete {
            case Success(response) =>
              writeAndClose(response.toBuffer)
            case Failure(exc) =>
              logError("Error occurred while processing request", Some(exc))
              val buffer = errorHandler(exc).toBuffer
              writeAndClose(buffer)
          }
          acceptLoop()
        case Failure(_: ClosedChannelException) =>
          // do nothing
        case Failure(exception) =>
          logError("Unable to accept client", Some(exception))
          acceptLoop()
      }
    }

    acceptLoop()
    serverChannel
  }

  private val DefaultInternalErrorResponse =
    HttpResponse.create(StatusCode.`Internal Server Error`, "Internal server error")

  private val DefaultNotFoundResponse = Future successful {
    HttpResponse.create(StatusCode.`Not Found`, "Internal server error")
  }

  private def errorHandler(throwable: Throwable): HttpResponse =
    DefaultInternalErrorResponse

  private def notFoundHandler(request: HttpRequest): Future[HttpResponse] =
    DefaultNotFoundResponse

  private def logError(message: String, throwable: Option[Throwable]): Unit = {
    System.err.println(message)
    throwable.foreach(_.printStackTrace(System.err))
  }
}
