import scoverage.ScoverageKeys
import uk.gov.hmrc.DefaultBuildSettings.integrationTestSettings

val appName = "find-my-nino-add-to-wallet"

val silencerVersion = "1.7.12"

addCommandAlias("report", ";clean; coverage; test; it:test; coverageReport")

lazy val microservice = Project(appName, file("."))
  .disablePlugins(JUnitXmlReportPlugin)
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .settings(
    majorVersion                     := 0,
    scalaVersion                     := "2.13.8",
    libraryDependencies              ++= AppDependencies.compile ++ AppDependencies.test,
    resolvers ++= Seq(
      Resolver.jcenterRepo,
      "HMRC-open-artefacts-maven".at("https://open.artefacts.tax.service.gov.uk/maven2")
    ),
    PlayKeys.playDefaultPort := 14005,
    // ***************
    // Use the silencer plugin to suppress warnings
    ScoverageKeys.coverageExcludedFiles := "<empty>;Reverse.*;.*handlers.*;" +
      ".*models.*;.*BuildInfo.*;.*Routes.*;.*javascript.*;.*GuiceInjector;.*AppConfig;.*Module;" +
      ".*ControllerConfiguration;",
    ScoverageKeys.coverageExcludedPackages := "<empty>;target.*",
    ScoverageKeys.coverageMinimumStmtTotal := 80,
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true,
    scalacOptions += "-P:silencer:pathFilters=routes",
    libraryDependencies ++= Seq(
      compilerPlugin("com.github.ghik" % "silencer-plugin" % silencerVersion cross CrossVersion.full),
      "com.github.ghik" % "silencer-lib" % silencerVersion % Provided cross CrossVersion.full
    )
    // ***************
  )
  .configs(IntegrationTest)
  .settings(integrationTestSettings(): _*)
  .settings(resolvers += Resolver.jcenterRepo)
  .settings(CodeCoverageSettings.settings: _*)
