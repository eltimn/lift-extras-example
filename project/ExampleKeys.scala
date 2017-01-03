import sbt._
import sbt.Keys._

object ExampleKeys {
  val liftVersion = settingKey[String]("Lift Web Framework full version number")
  val liftEdition = settingKey[String]("Lift Edition (such as 2.6 or 3.0)")
  val fullBuild = TaskKey[Unit]("full-build", "Do a full build.")
  val prBuild = TaskKey[Unit]("pr-build", "Do a pull request build.")
}
