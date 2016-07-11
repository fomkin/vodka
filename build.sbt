val publishSettings = Seq(
  publishMavenStyle := true,
  publishArtifact in Test := false,
  pomIncludeRepository := { _ => false },
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value) Some("snapshots" at s"${nexus}content/repositories/snapshots")
    else Some("releases" at s"${nexus}service/local/staging/deploy/maven2")
  },
  pomExtra := {
    <url>https://github.com/fomkin/vodka</url>
    <licenses>
      <license>
        <name>Apache License, Version 2.0</name>
        <url>http://apache.org/licenses/LICENSE-2.0</url>
        <distribution>repo</distribution>
      </license>
    </licenses>
    <scm>
      <url>git@github.com:fomkin/vodka.git</url>
      <connection>scm:git:git@github.com:fomkin/vodka.git</connection>
    </scm>
    <developers>
      <developer>
        <id>fomkin</id>
        <name>Aleksey Fomkin</name>
        <email>aleksey.fomkin@gmail.com</email>
      </developer>
    </developers>
  }
)

val commonSettings = publishSettings ++ Seq(
  organization := "com.github.fomkin",
  version := "0.1.1",
  libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.0-M15" % "test",
  scalacOptions ++= Seq(
    "-deprecation",
    "-feature",
    "-Xfatal-warnings",
    "-language:postfixOps",
    "-language:implicitConversions"
  )
)

lazy val http = project.
  settings(commonSettings: _*).
  settings(normalizedName := "vodka-http")

publishTo := Some(Resolver.file("Unused transient repository", file("target/unusedrepo")))

crossScalaVersions := Seq("2.10.6", "2.11.8")

publishArtifact := false
