package vodka

trait UserException extends Throwable {
  def statusCode: StatusCode
}
