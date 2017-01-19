package vodka.connection

import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.{AsynchronousSocketChannel, CompletionHandler}

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{Future, Promise}

/**
  * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
  */
final class AsynchronousSocketChannelWrapper(channel: AsynchronousSocketChannel) {

  import vodka.system.executionContext

  private[vodka] def handle[T](f: CompletionHandler[T, Unit] => Unit): Future[T] = {
    val p = Promise[T]()
    val ch = new CompletionHandler[T, Unit] {
      def completed(result: T, attachment: Unit): Unit = p.success(result)
      def failed(exc: Throwable, attachment: Unit): Unit = p.failure(exc)
    }
    f(ch)
    p.future
  }

  def connect(addr: InetSocketAddress, timeout: FiniteDuration): Future[ConnectedAsynchronousChannel] = {

    handle[Void](channel.connect[Unit](addr, (), _)) map { _ =>

      new ConnectedAsynchronousChannel {

        val address = addr

        def write(buffer: ByteBuffer, timeout: FiniteDuration): Future[Unit] = {
          def loop(alreadyWritten: Int): Future[Unit] = {
            handle[Integer](channel.write[Unit](buffer, timeout.length, timeout.unit, (), _)) flatMap {
              case count if count + alreadyWritten == buffer.capacity() => Future.successful(())
              case count => loop(count + alreadyWritten)
            }
          }
          loop(0)
        }

        def read[T](buffer: ByteBuffer, timeout: FiniteDuration)
          (f: Int => Either[Unit, T]): Future[T] = {
          def loop: Future[T] = {
            handle[Integer](channel.read[Unit](buffer, timeout.length, timeout.unit, (), _)) flatMap {
              count => f(count) match {
                case Left(_) => loop
                case Right(value) => Future.successful(value)
              }
            }
          }
          loop
        }
      }
    }
  }

}
