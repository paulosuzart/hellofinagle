name := "hellofinagle"

version := "1.0"

scalaVersion := "2.9.1"

fork in run := true

resolvers ++= Seq("Twitter Repo" at "http://maven.twttr.com/",
				  "Scala Tools Releases" at "https://repository.jboss.org/nexus/content/repositories/scala-tools-releases")

libraryDependencies ++= Seq("com.twitter" % "finagle-core" % "1.9.2",
							"com.twitter" % "finagle-http" % "1.9.2",
							"com.twitter" % "util" % "1.11.8",
							"org.mockito" % "mockito-all" % "1.8.5" % "test" withSources(),
							"org.scala-tools.testing" %% "specs" % "1.6.9" % "test" withSources())
