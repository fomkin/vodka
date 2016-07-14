package vodka

/**
  * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
  */
class UnsupportedCharsetException(charset: String)
    extends Exception(s"Unsupported charset: $charset")
    with UserException {
  def statusCode: StatusCode = StatusCode.`Bad Request`
}
