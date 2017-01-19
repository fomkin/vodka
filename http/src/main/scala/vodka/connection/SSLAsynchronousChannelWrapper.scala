package vodka.connection

import java.nio.ByteBuffer
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLEngineResult.{HandshakeStatus, Status}

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

/**
  * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
  */
class SSLAsynchronousChannelWrapper(
  channel: ConnectedAsynchronousChannel,
  sslContext: SSLContext) extends ConnectedAsynchronousChannel {

  val address = channel.address

  private val sslEngine = sslContext.createSSLEngine(address.getHostName, address.getPort)
  private val socketBufferSize = sslEngine.getSession.getPacketBufferSize
  private val socketReadBuffer = ByteBuffer.allocateDirect(socketBufferSize)
  private val socketWriteBuffer = ByteBuffer.allocateDirect(socketBufferSize)

  sslEngine.setUseClientMode(true)

//  def write(buffer: ByteBuffer, timeout: FiniteDuration): Future[Unit] = {
//    socketWriteBuffer.clear()
//
//    // Encrypt the data
//    val r = sslEngine.wrap(buffer, socketWriteBuffer)
//    val s = r.getStatus
//
//    println(s"$r = r; $s = s")
//
//    // Check for tasks
//    r.getHandshakeStatus match {
//      case HandshakeStatus.NEED_TASK =>
//        var runnable = sslEngine.getDelegatedTask
//        while (runnable != null) {
//          runnable.run()
//          runnable = sslEngine.getDelegatedTask
//        }
//    }
//
//    if (s == Status.OK || s == Status.BUFFER_OVERFLOW) {
//      // Need to write out the bytes and may need to read from
//      // the source again to empty it
//      socketWriteBuffer.flip()
//      channel.write(socketWriteBuffer, timeout)
//    } else {
//      // Status.BUFFER_UNDERFLOW - only happens on unwrap
//      // Status.CLOSED - unexpected
//      throw new IllegalStateException()
//    }
//  }
//
//  def read[T](buffer: ByteBuffer, timeout: FiniteDuration)
//    (f: Int => Either[Unit, T]): Future[T] = {
//
//    var readBytes = 0
//
//    channel.read(socketReadBuffer, timeout) { _ =>
//
//      socketReadBuffer.compact()
//      socketReadBuffer.flip()
//
//      // Decrypt the data in the buffer
//      val r  = sslEngine.unwrap(socketReadBuffer, buffer)
//      readBytes += r.bytesProduced()
//
//      // Check need task. Run on current thread cause it already
//      // executes on thread pool.
//      if (r.getHandshakeStatus == HandshakeStatus.NEED_TASK) {
//        var runnable = sslEngine.getDelegatedTask
//        while (runnable != null) {
//          runnable.run()
//          runnable = sslEngine.getDelegatedTask
//        }
//      }
//
//      // Check status
//      r.getStatus match {
//        case Status.OK =>
//          // Bytes available for reading and there may be
//          // sufficient data in the socketReadBuffer to
//          // support further reads without reading from the
//          // socket
//          val cnt = readBytes
//          readBytes = 0
//          f(cnt)
//        case Status.BUFFER_UNDERFLOW =>
//          // There is partial data in the socketReadBuffer
//          // else return the data we have and deal with the
//          // partial data on the next read
//          Left(())
//        case Status.BUFFER_OVERFLOW =>
//          // Not enough space in the destination buffer to
//          // store all of the data. We could use a bytes read
//          // value of -bufferSizeRequired to signal the new
//          // buffer size required but an explicit exception is
//          // clearer.
//          throw new Exception("Buffer overflow")
//        case _ =>
//          // Wrong state
//          throw new IllegalStateException()
//      }
//    }
//  }
}
