enablePlugins(ScalaJSPlugin)

name := "spgui"

version := "0.0.1"

scalaVersion := "2.11.8"

val scalaJSReactVersion = "0.11.3"
val scalaCssVersion = "0.5.1"
val diodeVersion = "1.1.0"

libraryDependencies ++= Seq(
  "com.github.japgolly.scalajs-react" %%% "core" % scalaJSReactVersion,
  "com.github.japgolly.scalajs-react" %%% "extra" % scalaJSReactVersion,
  "com.github.japgolly.scalacss" %%% "core" % scalaCssVersion,
  "com.github.japgolly.scalacss" %%% "ext-react" % scalaCssVersion,
  "me.chrons" %%% "diode" % diodeVersion,
  "me.chrons" %%% "diode-react" % diodeVersion,
  "com.lihaoyi" %%% "upickle" % "0.4.3",
  "fr.hmil" %%% "roshttp" % "2.0.0-RC1"
)

/* This is how to include js files. Put it in src/main/resources.
jsDependencies ++= Seq(
  ProvidedJS / "SomeJSFile.js"
)
*/