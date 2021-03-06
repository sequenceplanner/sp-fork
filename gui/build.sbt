enablePlugins(ScalaJSPlugin)

name := "spgui"

version := "2.1"

scalaOrganization := "org.scala-lang"

scalaVersion := "2.12.3"

//javaOptions += "-Xmx2048m"


scalacOptions  := Seq(
  "-encoding", "utf8",
  "-feature",
  "-unchecked",
  "-deprecation",
  "-language:implicitConversions",
  "-language:postfixOps"
)

val scalaJSReactVersion = "1.1.0"
val scalaCssVersion = "0.5.3"
val diodeVersion = "1.1.2"
val scalajsGoogleChartsVersion = "0.4.1"

resolvers += sbt.Resolver.url("aleastchs Bintray Releases", url("https://dl.bintray.com/aleastchs/aleastChs-releases"))(Resolver.ivyStylePatterns)

libraryDependencies ++= Seq(
  "com.github.japgolly.scalajs-react" %%% "core" % scalaJSReactVersion,
  "com.github.japgolly.scalajs-react" %%% "extra" % scalaJSReactVersion,
  "com.github.japgolly.scalacss" %%% "core" % scalaCssVersion,
  "com.github.japgolly.scalacss" %%% "ext-react" % scalaCssVersion,
  "io.suzaku" %%% "diode" % diodeVersion,
  "io.suzaku" %%% "diode-react" % diodeVersion,
  "com.lihaoyi" %%% "scalarx" % "0.3.2",
  "org.singlespaced" %%% "scalajs-d3" % "0.3.4",
  "org.scalatest" %%% "scalatest" % "3.0.1" % "test",
  "org.scala-lang.modules" %%% "scala-parser-combinators" % "1.0.5",
  "com.typesafe.play" %%% "play-json" % "2.6.0",
  "org.julienrf" %%% "play-json-derived-codecs" % "4.0.0",
  "io.github.cquiroz" %%% "scala-java-time" % "2.0.0-M12",
  "org.aleastChs" % "scalajs-google-charts_2.12" % scalajsGoogleChartsVersion
)

libraryDependencies += "org.scala-js" %%% "scalajs-java-time" % "0.2.2"

/* This is how to include js files. Put it in src/main/resources.
jsDependencies ++= Seq(
  ProvidedJS / "SomeJSFile.js"
)
*/
