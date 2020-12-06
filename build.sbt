lazy val dottytags = project.in(file("."))
  .aggregate(core.jvm, core.js)
  .settings(publish := {}, publishLocal := {})

lazy val core = crossProject(JVMPlatform, JSPlatform)
	.crossType(CrossType.Full)
	.in(file("."))
	.settings (
		name := "dottytags",
		version := "0.1.0",
		scalaVersion := "3.0.0-M2",
		scalacOptions ++= Seq(
			"-source:3.1-migration",
			"-indent", "-new-syntax", "-rewrite",
			"-Yexplicit-nulls", "-Ycheck-init", "-language:strictEquality", 
		)
	)
	.jvmSettings (
		libraryDependencies ++= Seq(
			"org.scalatest" % "scalatest_2.13" % "3.2.3" % Test,
		)
	)
	.jsSettings (
		libraryDependencies ++= Seq(
			"org.scalatest" % "scalatest_sjs1_2.13" % "3.2.3" % Test,
		),
		scalaJSUseMainModuleInitializer := true,
	)