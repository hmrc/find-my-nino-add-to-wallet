import scoverage.ScoverageKeys
import uk.gov.hmrc.DefaultBuildSettings

val appName = "find-my-nino-add-to-wallet"

ThisBuild / majorVersion := 0
ThisBuild / scalaVersion := "2.13.12"

addCommandAlias("report", ";clean; coverage; test; it/test; coverageReport")

lazy val microservice = Project(appName, file("."))
  .disablePlugins(JUnitXmlReportPlugin)
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .settings(
    libraryDependencies              ++= AppDependencies.compile ++ AppDependencies.test,
    resolvers ++= Seq(
      Resolver.jcenterRepo,
      "HMRC-open-artefacts-maven".at("https://open.artefacts.tax.service.gov.uk/maven2")
    ),
    PlayKeys.playDefaultPort := 14005,
    ScoverageKeys.coverageExcludedFiles := "<empty>;Reverse.*;.*handlers.*;" +
      ".*models.*;.*BuildInfo.*;.*Routes.*;.*javascript.*;.*GuiceInjector;.*AppConfig;.*Module;" +
      ".*ControllerConfiguration;.*AuditService.*;.*SignatureService.*;",
    ScoverageKeys.coverageExcludedPackages := "<empty>;target.*",
    ScoverageKeys.coverageMinimumStmtTotal := 90,
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true,
    scalacOptions ++= Seq(
      "-Wconf:cat=unused&src=.*routes/.*:s",
      "-Wconf:cat=deprecation&msg=trait HttpClient in package http is deprecated \\(since 15.0.0\\).*:s",
      "-Werror"
    )
  )
  .settings(resolvers += Resolver.jcenterRepo)
  .settings(CodeCoverageSettings.settings: _*)

lazy val it = project
  .enablePlugins(PlayScala)
  .dependsOn(microservice % "test->test") // the "test->test" allows reusing test code and test dependencies
  .settings(DefaultBuildSettings.itSettings())
  .settings(
    libraryDependencies ++= AppDependencies.test,
    DefaultBuildSettings.itSettings()
  )
