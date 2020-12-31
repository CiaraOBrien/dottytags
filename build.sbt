val printPhases = settingKey[Seq[String]]("The phases after which to print the tree")
val playgroundFilter = settingKey[FileFilter]("The files to print")

Global / onChangedBuildSource := ReloadOnSourceChanges

lazy val commonSettings = Seq (
	name := "dottytags",
	version := "0.2.0",
	scalaVersion := "3.0.0-M3",
	scalacOptions ++= Seq(
		"-source:3.1", "-indent", "-new-syntax",
		"-Yexplicit-nulls", "-Ycheck-init", "-language:strictEquality", "-Yindent-colons", "-Yerased-terms", 
	),
)

lazy val dottytags = project.in(file("."))
  .aggregate(core.jvm, core.js)
  .settings(
		publish := {},
		publishLocal := {},
		commands += Command.command("play") { state =>
			"playground / clean" ::
    	"playground / run"   :: 
      state
  	},
	)

lazy val core = crossProject(JVMPlatform, JSPlatform)
	.crossType(CrossType.Full)
	.in(file("."))
	.settings(commonSettings,
		libraryDependencies += "org.typelevel" %%% "cats-core" % "2.3.1",
		libraryDependencies += "io.monix" %%% "minitest" % "2.8.2-5ebd81f-SNAPSHOT" % "test",
		testFrameworks += new TestFramework("minitest.runner.Framework"),
		Test / parallelExecution := false
	)
	.jvmSettings (
		libraryDependencies += "com.lihaoyi" % "scalatags_2.13" % "0.9.2" % "test",
	)
	.jsSettings (
	  libraryDependencies += "com.lihaoyi" % "scalatags_sjs1_2.13" % "0.9.2",
		scalaJSUseMainModuleInitializer in Compile := true,
		mainClass in Compile := Some("test.TestMain"),
		jsEnv := new org.scalajs.jsenv.jsdomnodejs.JSDOMNodeJSEnv(),
	)

val typerPhase    = "typer"
val macrosPhase   = "staging"
val erasurePhase  = "erasure"
val lastPhase     = "collectSuperCalls"
val bytecodePhase = "genBCode"
lazy val playground = project.in(file("playground"))
  .dependsOn(core.jvm)
  .settings(commonSettings,
		printPhases := Seq(macrosPhase),
		logBuffered := true,
    scalacOptions ++= Seq("-Xprint:" + printPhases.value.mkString(","))
  )

