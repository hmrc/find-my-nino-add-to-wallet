import sbt.Setting
import scoverage.ScoverageKeys

object CodeCoverageSettings {

  private val excludedPackages: Seq[String] = Seq(
    "<empty>",
    "Reverse.*",
    "uk.gov.hmrc.BuildInfo",
    "app.*",
    "prod.*",
    ".*Routes.*",
    "controllers.testOnly.*",
    "testOnlyDoNotUseInAppConf.*",
    ".*\\$anon.*"
  )

  private val excludedFiles: Seq[String] = Seq(
    "<empty>",
    "Reverse.*",
    ".*handlers.*",
    ".*models.*",
    ".*BuildInfo.*",
    ".*Routes.*",
    ".*javascript.*",
    ".*GuiceInjector",
    ".*AppConfig",
    ".*HmrcModule",
    ".*AuditService.*",
    ".*SignatureService.*"
  )

  val settings: Seq[Setting[_]] = Seq(
    ScoverageKeys.coverageExcludedPackages := excludedPackages.mkString(";"),
    ScoverageKeys.coverageExcludedFiles := excludedFiles.mkString(";"),
    ScoverageKeys.coverageMinimumStmtTotal := 50,
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true
  )
}
