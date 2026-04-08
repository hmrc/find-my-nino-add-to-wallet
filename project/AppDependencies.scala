import sbt.*

object AppDependencies {

  private val mongoToggleVersion = "2.5.0"
  private val playVersion        = "play-30"
  private val bootstrapVersion   = "10.7.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"                  %% s"bootstrap-backend-$playVersion"            % bootstrapVersion,
    "org.bouncycastle"              % "bcpkix-jdk18on"                             % "1.83",
    "org.bouncycastle"              % "bcprov-jdk18on"                             % "1.83",
    "org.bouncycastle"              % "bcutil-jdk18on"                             % "1.83",
    "com.google.zxing"              % "core"                                       % "3.5.4",
    "com.nimbusds"                  % "nimbus-jose-jwt"                            % "10.9",
    "com.google.api-client"         % "google-api-client"                          % "2.9.0",
    "org.typelevel"                %% "cats-core"                                  % "2.13.0",
    "com.google.auth"               % "google-auth-library-oauth2-http"            % "1.43.0",
    "com.google.guava"              % "guava"                                      % "33.5.0-jre",
    "com.auth0"                     % "java-jwt"                                   % "4.5.1",
    "com.fasterxml.jackson.module" %% "jackson-module-scala"                       % "2.21.2",
    "uk.gov.hmrc"                  %% s"crypto-json-$playVersion"                  % "8.4.0",
    "uk.gov.hmrc"                  %% s"mongo-feature-toggles-client-$playVersion" % mongoToggleVersion,
    "uk.gov.hmrc"                  %% "domain-play-30"                             % "12.1.0"
  )

  val test: Seq[ModuleID] = Seq(
    "org.scalatestplus" %% "scalacheck-1-18"                                 % "3.2.19.0",
    "uk.gov.hmrc"       %% s"mongo-feature-toggles-client-test-$playVersion" % mongoToggleVersion
  ).map(_ % "test")
}
