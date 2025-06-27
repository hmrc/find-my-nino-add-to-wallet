import scoverage.ScoverageKeys
import uk.gov.hmrc.DefaultBuildSettings

val appName = "find-my-nino-add-to-wallet"

ThisBuild / majorVersion := 1
ThisBuild / scalaVersion := "3.3.5"
ThisBuild / scalafmtOnCompile := true

addCommandAlias("report", ";clean; coverage; test; it/test; coverageReport")

lazy val microservice = Project(appName, file("."))
  .disablePlugins(JUnitXmlReportPlugin)
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .settings(
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test,
    resolvers ++= Seq(
      "HMRC-open-artefacts-maven".at("https://open.artefacts.tax.service.gov.uk/maven2")
    ),
    PlayKeys.playDefaultPort := 14005,
    scalacOptions ++= Seq(
      "-Werror",
      "-unchecked",
      "-feature",
      "-language:noAutoTupling",
      "-Wvalue-discard",
      "-Wconf:msg=unused&src=.*routes/.*:s",
      "-Wconf:msg=unused&src=.*RoutesPrefix\\.scala:s",
      "-Wconf:msg=unused&src=.*Routes\\.scala:s",
      "-Wconf:msg=unused&src=.*ReverseRoutes\\.scala:s",
      "-Wconf:msg=Flag.*repeatedly:s"
    )
  )
  .settings(CodeCoverageSettings.settings: _*)

lazy val it = project
  .enablePlugins(PlayScala)
  .dependsOn(microservice % "test->test") // the "test->test" allows reusing test code and test dependencies
  .settings(DefaultBuildSettings.itSettings())
  .settings(
    libraryDependencies ++= AppDependencies.test,
    DefaultBuildSettings.itSettings()
  )
