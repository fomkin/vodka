package vodka

object Header {

  val CharsetPattern = """(.+);.*charset=(.+)""".r
  val JsonContentType = "application/json"

  val ContentLength = "content-length"
  val ContentType = "content-type"
  val Host = "host"
  val UserAgent = "user-agent"
  val Accept = "accept"

  private def appendHost(host: String)(hs: Map[String, String]) =
    if (!hs.contains(Host)) hs + (Host -> host)
    else hs

  private def appendUserAgent(hs: Map[String, String]) =
    if (!hs.contains(UserAgent)) hs + (UserAgent -> "vodka")
    else hs

  private def appenAccept(hs: Map[String, String]) =
    if (!hs.contains(Accept)) hs + (Accept -> "*/*")
    else hs

  def appendDefaultHeaders(host: String, hs: Map[String, String]) = {
    val f = appendHost(host) _ andThen appendUserAgent andThen appenAccept
    f(hs)
  }
}
