import sbt._

object AppDependencies {

  private val mongoToggleVersion = "1.9.0"
  private val playVersion = "play-30"
  private val bootstrapVersion = "9.10.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"             %% s"bootstrap-backend-$playVersion"      % bootstrapVersion,
    "org.bouncycastle"        % "bcpkix-jdk18on"                        % "1.78.1",
    "org.bouncycastle"        % "bcprov-jdk18on"                        % "1.78.1",
    "org.bouncycastle"        % "bcutil-jdk18on"                        % "1.78.1",
    "com.google.zxing"        % "core"                                  % "3.5.3",
    "com.nimbusds"            % "nimbus-jose-jwt"                       % "9.40",
    "com.google.api-client"   % "google-api-client"                     % "2.6.0",
    "com.google.zxing"        % "core"                                  % "3.5.3",
    "com.google.auth"         % "google-auth-library-oauth2-http"       % "1.23.0",
    "com.auth0"               % "java-jwt"                              % "4.4.0",
    "com.fasterxml.jackson.module"  %% "jackson-module-scala"           % "2.17.2",
    "uk.gov.hmrc"             %% s"crypto-json-$playVersion"            % "7.6.0",
    "uk.gov.hmrc"             %% s"mongo-feature-toggles-client-$playVersion" % mongoToggleVersion
  )

  val test: Seq[ModuleID] = Seq(
    "com.github.simplyscala"  % "scalatest-embedmongo_2.12"         % "0.2.4",
    "uk.gov.hmrc"             %% s"mongo-feature-toggles-client-test-$playVersion" % mongoToggleVersion,
    "com.vladsch.flexmark"    %  "flexmark-all"                     % "0.64.8",
    "org.mockito"             % "mockito-core"                      % "5.11.0",
    "org.mockito"             %% "mockito-scala"                    % "1.17.37",
    "org.mockito"             %% "mockito-scala-scalatest"          % "1.17.37"
  ).map(_ % "test")
}
