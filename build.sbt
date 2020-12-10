lazy val dottytags = project.in(file("."))
  .aggregate(core.jvm, core.js)
  .settings(publish := {}, publishLocal := {})

lazy val core = crossProject(JVMPlatform, JSPlatform)
	.crossType(CrossType.Full)
	.in(file("."))
	.settings (
		name := "dottytags",
		version := "0.2.0",
		scalaVersion := dottyLatestNightlyBuild.get,
		scalacOptions ++= Seq(
			"-source:3.1-migration", "-indent", "-new-syntax",
			"-Yexplicit-nulls", "-Ycheck-init", "-language:strictEquality", 
		),
		//libraryDependencies += "com.lihaoyi" % "utest_3.0.0-M2" % "0.7.5" % "test",
		//testFrameworks += new TestFramework("utest.runner.Framework"),
	)
	.jvmSettings (
		libraryDependencies += "com.lihaoyi" % "scalatags_2.13" % "0.9.2" % "test",
		Test / scalacOptions += "-Xprint:collectSuperCalls"
	)
	.jsSettings (
		libraryDependencies += "com.lihaoyi" % "scalatags_sjs1_2.13" % "0.9.2" % "test",
	)
