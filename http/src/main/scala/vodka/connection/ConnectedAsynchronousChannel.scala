package vodka.connection

import java.net.InetSocketAddress
import java.nio.ByteBuffer

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

trait ConnectedAsynchronousChannel {
  def address: InetSocketAddress
  def write(buffer: ByteBuffer, timeout: FiniteDuration): Future[Unit]
  def read[T](initialBuffer: ByteBuffer, timeout: FiniteDuration)
    (f: Int => Either[Unit, T]): Future[T]
}
