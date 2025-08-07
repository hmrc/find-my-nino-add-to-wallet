import sbt.*

object AppDependencies {

  private val mongoToggleVersion = "2.2.0"
  private val playVersion        = "play-30"
  private val bootstrapVersion   = "9.18.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"                  %% s"bootstrap-backend-$playVersion"            % bootstrapVersion,
    "org.bouncycastle"              % "bcpkix-jdk18on"                             % "1.81",
    "org.bouncycastle"              % "bcprov-jdk18on"                             % "1.81",
    "org.bouncycastle"              % "bcutil-jdk18on"                             % "1.81",
    "com.google.zxing"              % "core"                                       % "3.5.3",
    "com.nimbusds"                  % "nimbus-jose-jwt"                            % "10.4",
    "com.google.api-client"         % "google-api-client"                          % "2.8.0",
    "org.typelevel"                %% "cats-core"                                  % "2.13.0",
    "com.google.auth"               % "google-auth-library-oauth2-http"            % "1.37.1",
    "com.google.guava"              % "guava"                                      % "33.4.8-jre",
    "com.auth0"                     % "java-jwt"                                   % "4.5.0",
    "com.fasterxml.jackson.module" %% "jackson-module-scala"                       % "2.19.2",
    "uk.gov.hmrc"                  %% s"crypto-json-$playVersion"                  % "8.2.0",
    "uk.gov.hmrc"                  %% s"mongo-feature-toggles-client-$playVersion" % mongoToggleVersion,
    "uk.gov.hmrc"                  %% "domain-play-30"                             % "12.1.0"
  )

  val test: Seq[ModuleID] = Seq(
    "org.scalatestplus" %% "scalacheck-1-18"                                 % "3.2.19.0",
    "uk.gov.hmrc"       %% s"mongo-feature-toggles-client-test-$playVersion" % mongoToggleVersion
  ).map(_ % "test")
}
