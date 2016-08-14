# Introduction

Life in Russia is very hard. Usually we are drunk when Putin coerce us to write Scala code. So I decided to create a library with which even drunk bear can make microservice. Vodka is a small HTTP/1.1 server (less than 1000 lines of code) created without any dependencies on top of NIO2.

# Motivation

Let's be fair, Play and akka-http are too large and too complicated for making small services. When I searching for alternatives I found two libraries looks good to me: Colossus and Http4s. Both have simple zero boilerplate bootstrap and great routing API. But Colossus depends on Akka (not every service needs Akka) and Http4s depends on Scalaz (What if I want Cats?). Obviously, we need yet another service library!

# Installation

Give a drink vodka to your SBT

```scala
libraryDependencies += "com.github.fomkin" %% "vodka-http" % "0.2.1"
```

# Usage

```scala
import scala.concurrent.Future
import vodka._

object Main extends App {
  
  Vodka() {
    case request <| GET -> Root / "hello" / name =>
      var res = HttpResponse.Ok(s"Hello, comrade $name. Let's drink vodka.")
      Future.successful(res)
  }
  
}

// $ sbt run
// $ curl http://localhost:9090/hello/Ivan
// Hello, comrade Ivan. Let's drink vodka.
```

## Configuration

Vodka takes configuration via `Vodka` constructor.

| Argument           | Description                                        |
|--------------------|----------------------------------------------------|
| `host`             |  Host to bind. 0.0.0.0 by default.                 |
| `port`             | Port to bind. 8080 by default.                     |
| `logError`         | Function for error logging. `println` by default.  |
| `notFoundHandler`  | Response generator when no route was matched.      |
| `errorHandler`     | Response generator when exception was thrown.      |
| `maxContentLength` | Max size of input body                             |

# JSON with Pushka

See https://github.com/fomkin/pushka

```scala
libraryDependencies += "com.github.fomkin" %% "vodka-pushka" % "0.2.1"
```

```scala
import pushka._
import vodka.pushkaSupport._

@pushka
case class Person(name: String, age: Int)
 
Vodka() {
  case fromJson(body) <| POST -> Root / "hello" =>
    val person = read[Person](body)
    HttpResponse.Ok(person.name)
}
```

That's all. Enjoy and na zdorovie!
