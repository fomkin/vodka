# Introduction

Life in Russia is very hard. Usually we are drunk when Putin coerce us to write Scala code. So I decided to create a library with which even drunk bear can make microservice. Vodka is a small HTTP/1.1 server (less than 1000 lines of code) created without any dependencies on top of NIO2.

# Motivation

Let's be fair, Play and akka-http are too large and too complicated for making small services. When I searching for alternatives I found two libraries looks good to me: Colossus and Http4s. Both have simple zero boilerplate bootstrap and great routing API. But Colossus depends on Akka (not every service needs Akka) and Http4s depends on Scalaz (What if I want Cats?). Obviously, we need yet another service library!

# Installation

Give a drink vodka to your SBT

```scala
libraryDependencies += "com.github.fomkin" %% "vodka" % "0.1.0"
```

# Usage

```scala
import scala.concurrent.Future
import Vodka._

object Main extends App {
  
  Vodka() {
    case request <| GET -> Root / "hello" / name =>
      Future.successful(s"Hello, comrade $name. Let's drink vodka.")
  }
  
}

// $ sbt run
// $ curl http://localhost:9090/hello/Ivan
// Hello, comrade Ivan. Let's drink vodka.
```

## Configuration

```scala
object Main extends App {

  Vodka(host = Host,
        port = Port,
        logError = Function for error logging,
        errorResponse = Custom internal error,
        notFoundResponse = Custom 404 page) {
    // ...
  }
}  
```

That's all. Enjoy and na zdorovie!
