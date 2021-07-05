lazy val versionString = "3.0.0"

lazy val root = project.in(file("."))
.aggregate(dottytags.jvm, dottytags.js)
.settings(
  Compile / sources := Nil,
  Test    / sources := Nil,
  publish           := {},
  publishLocal      := {},
  taste := (tasty / Compile / taste).value
)

lazy val dottytags = crossProject(JVMPlatform, JSPlatform).crossType(CrossType.Pure).in(file("."))
.settings(
  name                  := "dottytags",
  description           := "An experimental reimplementation of Scalatags using Scala 3 macros to do the work at compile-time.",
  version               := "1.0.0",
  versionScheme         := Some("semver-spec"),
  scalaVersion          := versionString,
  organization          := "io.github.ciaraobrien",
  organizationName      := "Ciara O'Brien",
  organizationHomepage  := Some(url("https://ciaraobrien.github.io/")),
  developers            := List(Developer(id = "CiaraOBrien", name = "Ciara O'Brien",
      email = "ciaraobrienf@gmail.com", url = url("https://github.com/CiaraOBrien"))),
  startYear             := Some(2021),
  licenses              := List("MIT" -> url("https://github.com/CiaraOBrien/dottytags/blob/main/LICENSE")),
  scmInfo               := Some(ScmInfo(url("https://github.com/CiaraOBrien/dottytags"), "scm:git@github.com:CiaraOBrien/dottytags.git")),
  homepage              := Some(url("https://github.com/CiaraOBrien/dottytags")),
  publishMavenStyle     := true,
  pomIncludeRepository  := { _ => false },
  publishTo := {
    val nexus = "https://s01.oss.sonatype.org/"
    if (isSnapshot.value) Some("snapshots" at nexus + "content/repositories/snapshots")
    else Some("releases" at nexus + "service/local/staging/deploy/maven2")
  },
  libraryDependencies  ++= Seq(
      "io.monix" %%% "minitest" % "2.9.6" % "test",
      ("com.lihaoyi" %%% "scalatags" % "0.9.4").cross(CrossVersion.for3Use2_13),
  ),
  testFrameworks        += new TestFramework("minitest.runner.Framework"),
  parallelExecution     := false,
  crossTarget           := file("target"),
  scalacOptions        ++= Seq( "-indent", "-new-syntax", "-Yexplicit-nulls", "-language:strictEquality" ),
).jvmSettings(

).jsSettings(
  Compile / scalaJSUseMainModuleInitializer := true,
  jsEnv := new org.scalajs.jsenv.jsdomnodejs.JSDOMNodeJSEnv(),
)

val taste = taskKey[Unit]("Compile and run \"tasty\", the playground subproject, printing the AST at certain phases.")
// Names of useful compiler phases
val TyperPhase = "typer"
val MacrosPhase = "staging"
val ErasurePhase = "erasure"
val LastPhase = "collectSuperCalls"
val BytecodePhase = "genBCode"
// List of phases to print
val PhasesToPrint = Seq(MacrosPhase, BytecodePhase)

lazy val tasty = project.in(file("tasty"))
.dependsOn(dottytags.jvm)
.settings(
  name := "tasty-playground",
  scalaVersion := versionString,
  Compile / scalaSource := (ThisBuild / baseDirectory).value / "tasty",
  Test / unmanagedSources := Nil,
  Compile / logBuffered := true,
  Compile / scalacOptions += ("-Xprint:" + PhasesToPrint.mkString(",")),
  Compile / taste := Def.sequential(
    dottytags.jvm / Compile / compile,
    Compile / clean,
    (Compile / run).toTask("")
  ).value,
  publish := {}, publishLocal := {}, test := {}, doc := { file(".") }
)
