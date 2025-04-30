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

import models.admin.ApplePassCertificates2
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.{reset, when}
import play.api.Application
import play.api.inject.bind
import repositories.{ApplePassRepoTrait, ApplePassRepository, GooglePassRepoTrait, GooglePassRepository}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.mongoFeatureToggles.model.FeatureFlag
import uk.gov.hmrc.mongoFeatureToggles.services.FeatureFlagService
import uk.gov.hmrc.play.bootstrap.auth.DefaultAuthConnector
import util.SpecBase

import scala.concurrent.Future

class AppConfigSpec extends SpecBase {
  lazy val mockFeatureFlagService: FeatureFlagService = mock[FeatureFlagService]

  override implicit lazy val app: Application = localGuiceApplicationBuilder()
    .configure(
      "applePass.appleWWDRCA"                 -> "appleWWDRCA",
      "applePass.privateCertificate"          -> "privateCertificate",
      "applePass.privateCertificatePassword"  -> "privateCertificatePassword",
      "applePass.appleWWDRCA2"                -> "appleWWDRCA2",
      "applePass.privateCertificate2"         -> "privateCertificate2",
      "applePass.privateCertificatePassword2" -> "privateCertificatePassword2"
    )
    .overrides(
      bind[FeatureFlagService].toInstance(mockFeatureFlagService),
      bind(classOf[ApplePassRepoTrait]).to(classOf[ApplePassRepository]),
      bind(classOf[GooglePassRepoTrait]).to(classOf[GooglePassRepository]),
      bind(classOf[AuthConnector]).to(classOf[DefaultAuthConnector])
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
}
