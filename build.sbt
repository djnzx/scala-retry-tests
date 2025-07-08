import scala.collection.Seq

Global / onChangedBuildSource := ReloadOnSourceChanges

name := "scala-retry-tests"
version := "1.0.0"

scalaVersion := "2.13.16"

javacOptions := Seq("-source", "17", "-target", "17")

scalacOptions ++= Seq(
  "-encoding",
  "UTF-8",
  "-feature",
  "-deprecation",
  "-unchecked",
  "-language:postfixOps",
  "-language:higherKinds",
  "-language:existentials",
  "-Wconf:cat=other-match-analysis:error",
  "-Wunused",
  "-Ymacro-annotations",
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard",
  "-Ywarn-dead-code",
)

libraryDependencies ++= Seq(
  "org.typelevel"    %% "munit-cats-effect" % "2.1.0",
  "com.github.cb372" %% "cats-retry"        % "3.1.3",
  "com.lihaoyi"      %% "pprint"            % "0.9.0"
)
