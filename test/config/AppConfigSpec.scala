/*
 * Copyright 2025 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package config

import _root_.util.SpecBase
import models.admin.ApplePassCertificates2
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.{reset, when}
import play.api.Application
import play.api.inject.bind
import repositories.{ApplePassRepoTrait, ApplePassRepository, GooglePassRepoTrait, GooglePassRepository}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.crypto.{Decrypter, Encrypter}
import uk.gov.hmrc.mongoFeatureToggles.model.FeatureFlag
import uk.gov.hmrc.mongoFeatureToggles.services.FeatureFlagService
import uk.gov.hmrc.play.bootstrap.auth.DefaultAuthConnector

import scala.concurrent.Future

class AppConfigSpec extends SpecBase {
  private lazy val mockFeatureFlagService: FeatureFlagService     = mock[FeatureFlagService]
  private trait EncrypterDecrypter extends Encrypter with Decrypter
  private implicit val mockEncrypterDecrypter: EncrypterDecrypter = mock[EncrypterDecrypter]

  override implicit lazy val app: Application = localGuiceApplicationBuilder()
    .configure(
      "applePass.appleWWDRCA"                                     -> "appleWWDRCA",
      "applePass.privateCertificate"                              -> "privateCertificate",
      "applePass.privateCertificatePassword"                      -> "privateCertificatePassword",
      "applePass.appleWWDRCA2"                                    -> "appleWWDRCA2",
      "applePass.privateCertificate2"                             -> "privateCertificate2",
      "applePass.privateCertificatePassword2"                     -> "privateCertificatePassword2",
      "appName"                                                   -> "find-my-nino-add-to-wallet",
      "frontendServiceUrl"                                        -> "http://localhost:14006/save-your-national-insurance-number",
      "googlePass.issuerId"                                       -> "issuer-id-123",
      "googlePass.key"                                            -> "dummy-google-key",
      "googlePass.expiry"                                         -> 10,
      "googlePass.url"                                            -> "https://pay.google.com/gp/v/save/",
      "googlePass.origins"                                        -> "localhost:14006",
      "mongodb.timeToLiveInSeconds"                               -> 900,
      "mongodb.session-cache.timeToLiveInSeconds"                 -> 3600,
      "mongodb.encryption.key"                                    -> "encryption-key-123",
      "mongodb.encryption.enabled"                                -> true,
      "external-url.individual-details.auth-token"                -> "Bearer 1234567890",
      "external-url.individual-details.environment"               -> "ist0",
      "external-url.individual-details.originator-id"             -> "originatorId",
      "external-url.individual-details.protocol"                  -> "http",
      "external-url.individual-details.host"                      -> "localhost",
      "external-url.individual-details.port"                      -> "14011",
      "microservice.services.nps-crn-api.protocol"                -> "http",
      "microservice.services.nps-crn-api.host"                    -> "localhost",
      "microservice.services.nps-crn-api.port"                    -> "14011",
      "microservice.services.nps-crn-api.token"                   -> "dummy",
      "microservice.services.nps-crn-api.correlationId.key"       -> "correlationId",
      "microservice.services.nps-crn-api.govUkOriginatorId.key"   -> "gov-uk-originator-id",
      "microservice.services.nps-crn-api.govUkOriginatorId.value" -> "originator-value",
      "microservice.services.fandf.protocol"                      -> "http",
      "microservice.services.fandf.host"                          -> "localhost",
      "microservice.services.fandf.port"                          -> "9333"
    )
    .overrides(
      bind[FeatureFlagService].toInstance(mockFeatureFlagService),
      bind(classOf[ApplePassRepoTrait]).to(classOf[ApplePassRepository]),
      bind(classOf[GooglePassRepoTrait]).to(classOf[GooglePassRepository]),
      bind(classOf[AuthConnector]).to(classOf[DefaultAuthConnector]),
      bind[Encrypter with Decrypter].toInstance(mockEncrypterDecrypter)
    )
    .disable[HmrcModule]
    .build()

  lazy val sut: AppConfig = app.injector.instanceOf[AppConfig]

  override def beforeEach(): Unit =
    reset(mockFeatureFlagService)

  "applePass" must {
    "use first set of certs" when {
      "ApplePassCertificates2 is disabled" in {
        when(mockFeatureFlagService.get(ArgumentMatchers.eq(ApplePassCertificates2)))
          .thenReturn(Future.successful(FeatureFlag(ApplePassCertificates2, isEnabled = false)))

        sut.appleWWDRCA.futureValue mustBe "appleWWDRCA"
        sut.privateCertificate.futureValue mustBe "privateCertificate"
        sut.privateCertificatePassword.futureValue mustBe "privateCertificatePassword"
      }
    }

    "use second set of certs" when {
      "ApplePassCertificates2 is enabled" in {
        when(mockFeatureFlagService.get(ArgumentMatchers.eq(ApplePassCertificates2)))
          .thenReturn(Future.successful(FeatureFlag(ApplePassCertificates2, isEnabled = true)))

        sut.appleWWDRCA.futureValue mustBe "appleWWDRCA2"
        sut.privateCertificate.futureValue mustBe "privateCertificate2"
        sut.privateCertificatePassword.futureValue mustBe "privateCertificatePassword2"
      }
    }
  }

  "googlePass config" must {
    "expose the configured values" in {
      sut.googlePassIssuerId mustBe "issuer-id-123"
      sut.googlePassKey mustBe "dummy-google-key"
      sut.googlePassExpiryYears mustBe 10
      sut.googlePassAddUrl mustBe "https://pay.google.com/gp/v/save/"
      sut.googlePassOrigins mustBe "localhost:14006"
    }
  }

  "individual-details config" must {
    "build the service url correctly" in {
      sut.individualDetailsToken mustBe "Bearer 1234567890"
      sut.individualDetailsEnvironment mustBe "ist0"
      sut.individualDetailsOriginatorId mustBe "originatorId"
      sut.individualDetailsServiceUrl mustBe "http://localhost:14011"
    }
  }

  "nps-crn-api config" must {
    "build the service url and keys correctly" in {
      sut.npsCrnCorrelationIdKey mustBe "correlationId"
      sut.npsCrnOriginatorIdKey mustBe "gov-uk-originator-id"
      sut.npsCrnOriginatorIdValue mustBe "originator-value"
      sut.npsCrnToken mustBe "dummy"
      sut.npsCrnUrl mustBe "http://localhost:14011"
    }
  }

  "fandf config" must {
    "build the service url correctly" in {
      sut.fandfServiceUrl mustBe "http://localhost:9333"
    }
  }

  "mongodb config" must {
    "expose ttl and encryption settings" in {
      sut.cacheTtl mustBe 900L
      sut.sessionCacheTTLInSeconds mustBe 3600
      sut.encryptionKey mustBe "encryption-key-123"
      sut.encryptionEnabled mustBe true
    }
  }
}
