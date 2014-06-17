import sbt._
import Keys._
import spray.revolver.RevolverPlugin.Revolver


object BuildSettings {
  val VERSION = "0.1-SNAPSHOT"

  lazy val basicSettings = Seq(
    version               := VERSION,
    homepage              := Some(new URL("http://spray.io")),
    organization          := "se.sekvensa",
    description           := "Sequence Planner",
    scalaVersion          := "2.10.4",
    resolvers             ++= Dependencies.resolutionRepos,
    scalacOptions         := Seq(
      "-encoding", "utf8",
      "-feature",
      "-unchecked",
      "-deprecation",
      "-target:jvm-1.6",
      "-language:_",
      "-Xlog-reflective-calls"
    )
  ) ++ Revolver.settings

  
  
}