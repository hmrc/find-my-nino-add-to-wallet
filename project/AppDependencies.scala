import sbt._

object AppDependencies {

  private val mongoVersion = "1.1.0"

  val compile = Seq(
    "uk.gov.hmrc"             %% "bootstrap-backend-play-28"  % "7.1.0",
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-play-28"         % mongoVersion,
    "org.bouncycastle"        % "bcpkix-jdk18on"              % "1.71",
    "org.bouncycastle"        % "bcprov-jdk18on"              % "1.71",
    "org.bouncycastle"        % "bcutil-jdk18on"              % "1.71",
    "com.google.zxing"        % "core"                        % "3.5.0",
    "com.nimbusds"            % "nimbus-jose-jwt"             % "9.25.1",
    "uk.gov.hmrc"             %% "crypto"                     % "7.2.0"
  )

  val test = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-28"     % "7.1.0"             % "test, it",
    "com.github.simplyscala"  % "scalatest-embedmongo_2.12"   % "0.2.4"             % "test",
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-test-play-28"    % mongoVersion        % Test,
    "com.vladsch.flexmark"    %  "flexmark-all"               % "0.36.8"            % "test, it",
    "org.mockito"             % "mockito-core"                % "4.0.0"             % "test",
    "org.mockito"             %% "mockito-scala"              % "1.16.42"           % "test",
    "org.mockito"             %% "mockito-scala-scalatest"    % "1.16.42"           % "test"
  )
}
