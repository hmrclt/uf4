import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}

name := "parent"

lazy val root = project.in(file("."))
  .aggregate(
    core.js,
    core.jvm,
    `common-web`.js,
    `common-web`.jvm,
    `interpreter-play`.projects(Play25),
    `interpreter-play`.projects(Play26),
    `interpreter-play`.projects(Play27)
  )
  .settings(
    publishLocal := {},
    publish := {},
    publishArtifact := false
  )

scalaVersion := "2.11.12"
crossScalaVersions := Seq("2.11.12")

val commonSettings = Seq(
  organization := "com.luketebbs.uniform",  
  scalaVersion := "2.12.8",
  crossScalaVersions := Seq("2.11.12", "2.12.8"),
  scalacOptions ++= ProjectSettings.defaultScalaCopts,
  scalacOptions in (Compile, console) --= Seq(
    "-Ywarn-unused:imports",
    "-Xfatal-warnings",
    "-Ywarn-unused"
  ),
  libraryDependencies ++= Seq(
    "org.scalatest" %%% "scalatest" % "3.0.5" % "test"
  ),
  addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.3"),
  wartremoverWarnings in (Compile, compile) ++= Warts.all
)

lazy val core = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      "org.scala-lang" % "scala-reflect" % scalaVersion.value,
      "org.typelevel" %%% "cats-core" % "1.6.0",
      "org.scala-lang.modules" %%% "scala-parser-combinators" % "1.1.1",
      "com.chuusai" %%% "shapeless" % "2.3.3",
      "com.github.mpilquist" %%% "simulacrum" % "0.18.0"      
    ),
    addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)  
  )

lazy val `common-web` = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Full)
  .settings(commonSettings)
  .settings(
    addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full),
    libraryDependencies ++= Seq(
      "com.chuusai" %%% "shapeless" % "2.3.3",
      "com.github.mpilquist" %%% "simulacrum" % "0.18.0"
    )
  )
  .dependsOn(core)

lazy val `interpreter-play`: sbtcrossproject.CrossProject =
  crossProject(Play25, Play26, Play27)
    .crossType(CrossType.Full)
    .settings(commonSettings)
    .configurePlatform(Play25)(_.settings(
      name := "interpreter-play25",
      scalaVersion := "2.11.12",
      crossScalaVersions := Seq("2.11.12")
    ).dependsOn(core.jvm, `common-web`.jvm))
    .configurePlatform(Play26)(_.settings(
      name := "interpreter-play26"
    ).dependsOn(core.jvm, `common-web`.jvm))
    .configurePlatform(Play27)(_.settings(
      name := "interpreter-play27"
    ).dependsOn(core.jvm, `common-web`.jvm))

lazy val `interpreter-cli` = project
  .settings(commonSettings)
  .dependsOn(core.jvm, `common-web`.jvm)

lazy val `example-play` = project.settings(commonSettings)
  .enablePlugins(PlayScala)
  .dependsOn(`interpreter-play`.projects(Play26), core.jvm)
  .settings(
    TwirlKeys.templateImports ++= Seq(
      "ltbs.uniform._",
      "ltbs.uniform.interpreters.playframework._"
    ),    
    PlayKeys.playDefaultPort := 9001,
    libraryDependencies ++= Seq(
      filters,
      guice
    ),
    libraryDependencies ++= Seq(
      "org.scalatest" %%% "scalatest" % "3.0.5" % "test",
      "uk.gov.hmrc" %% "play-nunjucks-spike" % "0.4.0-SNAPSHOT"
    ),
    initialCommands in console := "import cats.implicits._; import ltbs.uniform._; import ltbs.uniform.interpreters.playframework._; implicit val messages: Messages = NoopMessages",
    initialCommands in consoleQuick := """import cats.implicits._;"""
  )
