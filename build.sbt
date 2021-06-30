ThisBuild / bintrayReleaseOnPublish := false
ThisBuild / publishMavenStyle := true

lazy val root = project.in(file("."))
.aggregate(dottytags.jvm)
.settings(
  Compile / sources := Nil,
  Test    / sources := Nil,
  publish           := {},
  publishLocal      := {},
  taste := (tasty / Compile / taste).value
)

lazy val cross = root.aggregate(dottytags.jvm, dottytags.js)

lazy val dottytags = crossProject(JVMPlatform, JSPlatform).crossType(CrossType.Pure).in(file("."))
.settings(
  name                 := "dottytags",
  version              := "0.4.0",
  scalaVersion         := "3.0.0",
  developers           := List(
    Developer(id = "CiaraOBrien", name = "Ciara O'Brien",
              email = "ciaraobrienf@gmail.com", url = url("https://github.com/CiaraOBrien")),
  ),
  startYear            := Some(2021),
  licenses             := List("MIT" -> url("https://github.com/CiaraOBrien/dottytags/blob/main/LICENSE")),
  scmInfo              := Some(ScmInfo(url("https://github.com/CiaraOBrien/dottytags"), "scm:git@github.com:CiaraOBrien/dottytags.git")),
  homepage             := Some(url("https://github.com/CiaraOBrien/dottytags")),
  publishMavenStyle    := true,
  libraryDependencies ++= Seq(
     "io.monix"       %%% "minitest" % "2.9.6" % "test",
  ),
  testFrameworks    += new TestFramework("minitest.runner.Framework"),
  parallelExecution := false,
  crossTarget       := file("target"),
  scalacOptions    ++= Seq(
    "-Yexplicit-nulls", "-language:strictEquality",
  )
).jvmSettings(

).jsSettings(
  Compile / scalaJSUseMainModuleInitializer := true,
	jsEnv := new org.scalajs.jsenv.jsdomnodejs.JSDOMNodeJSEnv(),
)

val typerPhase    = "typer"
val macrosPhase   = "staging"
val erasurePhase  = "erasure"
val lastPhase     = "collectSuperCalls"
val bytecodePhase = "genBCode"
val printPhases   = Seq(macrosPhase, bytecodePhase)
val taste         = taskKey[Unit]("Clean and run \"tasty\"")

// Anything in this project can depend on the full Dottytags, and gets compiled and
// its intermediate AST phases printed by the compiler for debugging and general development assistance.
lazy val tasty = project.in(file("tasty"))
.dependsOn(dottytags.jvm)
.settings(
  name := "tasty-playground",
  scalaVersion := "3.0.0",
  Compile / scalaSource      := (ThisBuild / baseDirectory).value / "tasty",
  Test    / unmanagedSources := Nil,
  Compile / logBuffered      := true,
  Compile / scalacOptions    += ("-Xprint:" + printPhases.mkString(",")),
  Compile / taste := Def.sequential(
    dottytags.jvm / Compile / compile,
    Compile / clean,
    (Compile / run).toTask("")
  ).value,
  publish := {}, publishLocal := {}, test := {}, doc := { file(".") }
)
