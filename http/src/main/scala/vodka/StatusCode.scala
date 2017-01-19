package vodka

import scala.annotation.tailrec

sealed abstract class StatusCode(val value: Int, val reasonPhrase: String) {
  override def hashCode(): Int = value.hashCode
  override def equals(obj: scala.Any): Boolean = obj match {
    case StatusCode(`value`, _) => true
    case _ => false
  }
}

object StatusCode {

  def apply(value: Int, reasonPhrase: String): StatusCode =
    new StatusCode(value, reasonPhrase) {}

  def apply(value: Int): StatusCode = (value: @tailrec) match {
    case 100 => `Continue`
    case 101 => `Switching Protocols`
    case 200 => `OK`
    case 201 => `Created`
    case 202 => `Accepted`
    case 203 => `Non-Authoritative Information`
    case 204 => `No Content`
    case 205 => `Reset Content`
    case 206 => `Partial Content`
    case 300 => `Multiple Choices`
    case 301 => `Moved Permanently`
    case 302 => `Found`
    case 303 => `See Other`
    case 304 => `Not Modified`
    case 305 => `Use Proxy`
    case 307 => `Temporary Redirect`
    case 400 => `Bad Request`
    case 401 => `Unauthorized`
    case 402 => `Payment Required`
    case 403 => `Forbidden`
    case 404 => `Not Found`
    case 405 => `Method Not Allowed`
    case 406 => `Not Acceptable`
    case 407 => `Proxy Authentication Required`
    case 408 => `Request Time-out`
    case 409 => `Conflict`
    case 410 => `Gone`
    case 411 => `Length Required`
    case 412 => `Precondition Failed`
    case 413 => `Request Entity Too Large`
    case 414 => `Request-URI Too Large`
    case 415 => `Unsupported Media Type`
    case 416 => `Requested range not satisfiable`
    case 417 => `Expectation Failed`
    case 500 => `Internal Server Error`
    case 501 => `Not Implemented`
    case 502 => `Bad Gateway`
    case 503 => `Service Unavailable`
    case 504 => `Gateway Time-out`
    case 505 => `HTTP Version not supported`
  }
  
  def unapply(arg: StatusCode): Option[(Int, String)] = {
    Some((arg.value, arg.reasonPhrase))
  }

  case object `Continue` extends StatusCode(100, "Continue")
  case object `Switching Protocols` extends StatusCode(101, "Switching Protocols")
  case object `OK` extends StatusCode(200, "OK")
  case object `Created` extends StatusCode(201, "Created")
  case object `Accepted` extends StatusCode(202, "Accepted")
  case object `Non-Authoritative Information` extends StatusCode(203, "Non-Authoritative Information")
  case object `No Content` extends StatusCode(204, "No Content")
  case object `Reset Content` extends StatusCode(205, "Reset Content")
  case object `Partial Content` extends StatusCode(206, "Partial Content")
  case object `Multiple Choices` extends StatusCode(300, "Multiple Choices")
  case object `Moved Permanently` extends StatusCode(301, "Moved Permanently")
  case object `Found` extends StatusCode(302, "Found")
  case object `See Other` extends StatusCode(303, "See Other")
  case object `Not Modified` extends StatusCode(304, "Not Modified")
  case object `Use Proxy` extends StatusCode(305, "Use Proxy")
  case object `Temporary Redirect` extends StatusCode(307, "Temporary Redirect")
  case object `Bad Request` extends StatusCode(400, "Bad Request")
  case object `Unauthorized` extends StatusCode(401, "Unauthorized")
  case object `Payment Required` extends StatusCode(402, "Payment Required")
  case object `Forbidden` extends StatusCode(403, "Forbidden")
  case object `Not Found` extends StatusCode(404, "Not Found")
  case object `Method Not Allowed` extends StatusCode(405, "Method Not Allowed")
  case object `Not Acceptable` extends StatusCode(406, "Not Acceptable")
  case object `Proxy Authentication Required` extends StatusCode(407, "Proxy Authentication Required")
  case object `Request Time-out` extends StatusCode(408, "Request Time-out")
  case object `Conflict` extends StatusCode(409, "Conflict")
  case object `Gone` extends StatusCode(410, "Gone")
  case object `Length Required` extends StatusCode(411, "Length Required")
  case object `Precondition Failed` extends StatusCode(412, "Precondition Failed")
  case object `Request Entity Too Large` extends StatusCode(413, "Request Entity Too Large")
  case object `Request-URI Too Large` extends StatusCode(414, "Request-URI Too Large")
  case object `Unsupported Media Type` extends StatusCode(415, "Unsupported Media Type")
  case object `Requested range not satisfiable` extends StatusCode(416, "Requested range not satisfiable")
  case object `Expectation Failed` extends StatusCode(417, "Expectation Failed")
  case object `Internal Server Error` extends StatusCode(500, "Internal Server Error")
  case object `Not Implemented` extends StatusCode(501, "Not Implemented")
  case object `Bad Gateway` extends StatusCode(502, "Bad Gateway")
  case object `Service Unavailable` extends StatusCode(503, "Service Unavailable")
  case object `Gateway Time-out` extends StatusCode(504, "Gateway Time-out")
  case object `HTTP Version not supported` extends StatusCode(505, "HTTP Version not supported")
}
