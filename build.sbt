import sbt._

val appName = "git-client"

lazy val library = Project(appName, file("."))
  .enablePlugins(SbtAutoBuildPlugin, SbtGitVersioning, SbtArtifactory)
  .settings(
    majorVersion                     := 0,
    makePublicallyAvailableOnBintray := true
  )
  .settings(
    scalaVersion              := "2.11.6",
    parallelExecution in Test := false,
    fork in Test              := false,
    retrieveManaged           := true,
    libraryDependencies       ++= LibDependencies.compile ++ LibDependencies.test
  )
