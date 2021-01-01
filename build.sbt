Global / onChangedBuildSource := ReloadOnSourceChanges

lazy val commonSettings = Seq (
  name           := "dottytags",
  version        := "0.2.0",
  scalaVersion   := "3.0.0-M3",
)

lazy val dottytags = crossProject(JVMPlatform, JSPlatform).crossType(CrossType.Pure).in(file("."))
.settings(commonSettings,
  libraryDependencies += "org.typelevel" %%% "cats-core" % "2.3.1",
  libraryDependencies += "io.monix" %%% "minitest" % "2.8.2-5ebd81f-SNAPSHOT" % "test",
  testFrameworks += new TestFramework("minitest.runner.Framework"),
  Test / parallelExecution := false,
  //mainClass := Some("test.TestMain"),
)
.jvmSettings(
  libraryDependencies += "com.lihaoyi" % "scalatags_2.13"      % "0.9.2" % "test",
)
.jsSettings(
  libraryDependencies += "com.lihaoyi" % "scalatags_sjs1_2.13" % "0.9.2" % "test",
  scalaJSUseMainModuleInitializer in Compile := true,
	jsEnv := new org.scalajs.jsenv.jsdomnodejs.JSDOMNodeJSEnv(),
)

val typerPhase    = "typer"
val macrosPhase   = "staging"
val erasurePhase  = "erasure"
val lastPhase     = "collectSuperCalls"
val bytecodePhase = "genBCode"
val printPhases   = Seq(macrosPhase)

val taste = taskKey[Unit]("Clean and run \"tasty\"")

lazy val tasty = project.in(file("."))
.dependsOn(dottytags.jvm)
.settings(commonSettings,
  Compile / scalaSource   := baseDirectory.value / "tasty",
  Compile / logBuffered   := true,
  Compile / scalacOptions += ("-Xprint:" + printPhases.mkString(",")),
  Compile / taste := Def.sequential(
    Compile / clean,
    (Compile / run).toTask("")
  ).value,
  publish := {}, publishLocal := {}, test := {},
)
