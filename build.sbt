import ExampleKeys.{liftEdition, liftVersion}

scalaVersion := "2.12.1"
scalacOptions ++= Seq("-unchecked", "-deprecation")
parallelExecution in Test := false
organization in ThisBuild := "net.liftmodules"
liftVersion := "3.0.1"
liftEdition := liftVersion.value.split('.').take(2).mkString(".")

val jettyVersion = "9.3.8.v20160314"

libraryDependencies ++= {
  def lv(s: String): String = s"${s}_${liftEdition.value}"

  "net.liftweb" %% "lift-webkit" % liftVersion.value ::
  "net.liftweb" %% "lift-record" % liftVersion.value ::
  "net.liftmodules" %% lv("extras") % "0.7" ::
  "org.eclipse.jetty" % "jetty-server" % jettyVersion ::
  "org.eclipse.jetty" % "jetty-webapp" % jettyVersion ::
  "ch.qos.logback" % "logback-classic" % "1.1.2" ::
  "org.scalatest" %% "scalatest" % "3.0.1" % "test" ::
  Nil
}

enablePlugins(FrontendPlugin)

Revolver.settings

mainClass in Compile := Some("code.WebApp")

lazy val assetTarget = settingKey[File]("Asset target directory")
assetTarget := (target in Compile).value / "frontend"
cleanFiles += assetTarget.value

// add asset manifest to classpath
unmanagedResourceDirectories in Compile += assetTarget.value / "resources"

// assets
lazy val assetPackage = taskKey[Unit]("Package assets for deployment")
lazy val assetPrepare = taskKey[Unit]("Prepare asset target directory")

assetPackage := gulp.toTask(" dist --dist").value
assetPrepare := {
  IO.createDirectory(assetTarget.value / "dist")
}

lazy val start = taskKey[Unit]("Start the app")
start := reStart.toTask("").dependsOn(assetPrepare).value

addCommandAlias("stop", "reStop")

packSettings
packGenerateWindowsBatFile := false
packMain := Map("example" -> "code.WebApp")
packResourceDir += ((sourceDirectory in Compile).value / "webapp" -> "webapp")
packResourceDir += (assetTarget.value / "dist" -> "assets")

ExampleKeys.fullBuild := {
  val a = (test in Test).value
  val b = assetPackage.value
  ()
}

ExampleKeys.prBuild := {
  val a = (test in Test).value
  val b = gulp.toTask(" default").value
  ()
}
