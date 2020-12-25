lazy val dottytags = project.in(file("."))
  .aggregate(core.jvm, core.js)
  .settings(publish := {}, publishLocal := {})

lazy val core = crossProject(JVMPlatform, JSPlatform)
	.crossType(CrossType.Full)
	.in(file("."))
	.settings (
		name := "dottytags",
		version := "0.2.0",
		scalaVersion := "3.0.0-M3",
		scalacOptions ++= Seq(
			"-source:3.1-migration", "-indent", "-new-syntax",
			"-Yexplicit-nulls", "-Ycheck-init", "-language:strictEquality", 
		),
		libraryDependencies += "io.monix" %%% "minitest" % "2.8.2-5ebd81f-SNAPSHOT" % "test",
		testFrameworks += new TestFramework("minitest.runner.Framework")
	)
	.jvmSettings (
		libraryDependencies += "com.lihaoyi" % "scalatags_2.13" % "0.9.2" % "test",
		Test / scalacOptions += "-Xprint:collectSuperCalls",
	)
	.jsSettings (
		libraryDependencies += "com.lihaoyi" % "scalatags_sjs1_2.13" % "0.9.2" % "test",
		
	)
