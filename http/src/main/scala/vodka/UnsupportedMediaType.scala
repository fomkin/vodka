package vodka

/**
  * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
  */
class UnsupportedMediaType(expected: String, given: String)
    extends Exception(
        s"Unsupported media type. `$expected` expected, but `$given` given")
    with UserException {
  def statusCode = StatusCode.`Unsupported Media Type`
}
