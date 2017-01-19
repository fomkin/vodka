package vodka

import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousSocketChannel
import java.nio.charset.StandardCharsets
import java.security.cert.X509Certificate
import java.security.{NoSuchAlgorithmException, SecureRandom}
import javax.net.ssl.{SSLContext, X509TrustManager}

import vodka.connection.{AsynchronousSocketChannelWrapper, SSLAsynchronousChannelWrapper}

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success}

object VodkaClient {

  import system.executionContext

  def test = {
    val request = HttpRequest("GET", "/", "", Map(), StandardCharsets.UTF_8)
    run("example.com", 443, request, useSsl = true, 5 seconds) onComplete {
      case Success(x) => println(x.stringBody())
      case Failure(e) => e.printStackTrace()
    }
  }

  def run(host: String, port: Int, httpRequest: HttpRequest, useSsl: Boolean, phaseTimeout: FiniteDuration): Future[HttpResponse] = {
    val httpRequestWithDefaultHeaders = httpRequest.copy(
      headers = Header.appendDefaultHeaders(host, httpRequest.headers)
    )
    val requestBuffer = httpRequestWithDefaultHeaders.toBuffer
    val nioChannel = AsynchronousSocketChannel.open(system.asyncChannelGroup)
    val address = new InetSocketAddress(host, port)

    for {
      rawChannel <- new AsynchronousSocketChannelWrapper(nioChannel).connect(address, phaseTimeout)
      channel = if (useSsl) new SSLAsynchronousChannelWrapper(rawChannel, Foo.clientSSLContext()) else rawChannel
      _ <- channel.write(requestBuffer, phaseTimeout)
      responseHeaderBuffer = ByteBuffer.allocate(MAX_REQUEST_LINE_AND_HEADERS)
      partialResponse <- channel.read(responseHeaderBuffer, phaseTimeout) { count =>
        HttpResponse.fromBuffer(responseHeaderBuffer, Int.MaxValue) match {
          case Some(request) => Right(request)
          case None => Left(())
        }
      }
      response <- channel.read(partialResponse.body, phaseTimeout) { count =>
        if (count == 0) Right(partialResponse)
        else Left(())
      }
    } yield {
      //channel.close()
      response
    }
  }

}

//trait VodkaHttpTrnasport {
//
//  import system.executionContext
//
//  private[vodka] def handle[T](f: CompletionHandler[T, Unit] => Unit): Future[T] = {
//    val p = Promise[T]()
//    val ch = new CompletionHandler[T, Unit] {
//      def completed(result: T, attachment: Unit): Unit = p.success(result)
//      def failed(exc: Throwable, attachment: Unit): Unit = p.failure(exc)
//    }
//    f(ch)
//    p.future
//  }
//
//  def connectChannel(address: SocketAddress, channel: AsynchronousSocketChannel): Future[Void] = {
//    handle[Void](channel.connect[Unit](address, (), _))
//  }
//
//  def writeRequest(channel: AsynchronousSocketChannel, requestBuffer: ByteBuffer, timeout: FiniteDuration): Future[Unit] = {
//    def loop(alreadyWritten: Int): Future[Unit] = {
//      handle[Integer](channel.write[Unit](requestBuffer, timeout.length, timeout.unit, (), _)) flatMap {
//        case count if count + alreadyWritten == requestBuffer.capacity() => Future.successful(())
//        case count => loop(count + alreadyWritten)
//      }
//    }
//    loop(0)
//  }
//
//  def readResponseHeader(channel: AsynchronousSocketChannel, timeout: FiniteDuration): Future[HttpResponse] = {
//    val responseHeaderBuffer = ByteBuffer.allocate(MAX_REQUEST_LINE_AND_HEADERS)
//    def loop(): Future[HttpResponse] = {
//      handle[Integer](channel.read[Unit](responseHeaderBuffer, timeout.length, timeout.unit, (), _)) flatMap { _ =>
//        HttpResponse.fromBuffer(responseHeaderBuffer, Int.MaxValue) match {
//          case Some(request) => Future.successful(request)
//          case None => loop()
//        }
//      }
//    }
//    loop()
//  }
//
//  def readResponseBody(channel: AsynchronousSocketChannel, response: HttpResponse, timeout: FiniteDuration): Future[HttpResponse] = {
//    def loop(): Future[HttpResponse] = {
//      handle[Integer](channel.read[Unit](response.body, timeout.length, timeout.unit, (), _)) flatMap { l =>
//        if (l == 0) Future.successful(response)
//        else loop()
//      }
//    }
//    loop()
//  }
//}

object Foo {

  private class DefaultTrustManager extends X509TrustManager {
    def getAcceptedIssuers: Array[X509Certificate] =  new Array[java.security.cert.X509Certificate](0)
    def checkClientTrusted(certs: Array[X509Certificate], authType: String) { }
    def checkServerTrusted(certs: Array[X509Certificate], authType: String) { }
  }

  /** Create a default SSLContext with an empty trust manager */
  def clientSSLContext(): SSLContext = {
    try {
      val sslContext = SSLContext.getInstance("TLS")
      sslContext.init(null, Array(new DefaultTrustManager()), new SecureRandom())
      sslContext
    } catch {
      case e: NoSuchAlgorithmException => throw new ExceptionInInitializerError(e)
      case e: ExceptionInInitializerError => throw new ExceptionInInitializerError(e)
    }
  }
}
