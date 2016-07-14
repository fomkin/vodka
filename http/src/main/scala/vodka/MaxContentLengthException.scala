package vodka

class MaxContentLengthException
    extends Exception("Max content length reached")
    with UserException {
  def statusCode = StatusCode.`Bad Request`
}
