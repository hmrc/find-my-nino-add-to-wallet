import sbt._

object AppDependencies {

  private val mongoToggleVersion = "1.10.0"
  private val playVersion = "play-30"
  private val bootstrapVersion = "9.11.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"             %% s"bootstrap-backend-$playVersion"      % bootstrapVersion,
    "org.bouncycastle"        % "bcpkix-jdk18on"                        % "1.80",
    "org.bouncycastle"        % "bcprov-jdk18on"                        % "1.80",
    "org.bouncycastle"        % "bcutil-jdk18on"                        % "1.80",
    "com.google.zxing"        % "core"                                  % "3.5.3",
    "com.nimbusds"            % "nimbus-jose-jwt"                       % "10.2",
    "com.google.api-client"   % "google-api-client"                     % "2.7.2",
    "com.google.auth"         % "google-auth-library-oauth2-http"       % "1.33.1",
    "com.auth0"               % "java-jwt"                              % "4.5.0",
    "com.fasterxml.jackson.module"  %% "jackson-module-scala"           % "2.18.3",
    "uk.gov.hmrc"             %% s"crypto-json-$playVersion"            % "8.2.0",
    "uk.gov.hmrc"             %% s"mongo-feature-toggles-client-$playVersion" % mongoToggleVersion
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"             %% s"bootstrap-test-$playVersion"  % bootstrapVersion,
    "org.scalatestplus"       %% "scalacheck-1-18"               % "3.2.19.0",
    "uk.gov.hmrc"             %% s"mongo-feature-toggles-client-test-$playVersion" % mongoToggleVersion,
  ).map(_  % "test")
}
