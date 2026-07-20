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
import play.api.Application
import play.api.inject.bind
import repositories.{ApplePassRepoTrait, ApplePassRepository, GooglePassRepoTrait, GooglePassRepository}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.crypto.{Decrypter, Encrypter}
import uk.gov.hmrc.play.bootstrap.auth.DefaultAuthConnector

class AppConfigSpec extends SpecBase {
  private trait EncrypterDecrypter extends Encrypter with Decrypter
  private implicit val mockEncrypterDecrypter: EncrypterDecrypter = mock[EncrypterDecrypter]

  override implicit lazy val app: Application = localGuiceApplicationBuilder()
    .configure(
      "applePass.appleWWDRCA"                -> "appleWWDRCA",
      "applePass.privateCertificate"         -> "privateCertificate",
      "applePass.privateCertificatePassword" -> "privateCertificatePassword",
      "applePass.signingEnabled"             -> false
    )
    .overrides(
      bind(classOf[ApplePassRepoTrait]).to(classOf[ApplePassRepository]),
      bind(classOf[GooglePassRepoTrait]).to(classOf[GooglePassRepository]),
      bind(classOf[AuthConnector]).to(classOf[DefaultAuthConnector]),
      bind[Encrypter with Decrypter].toInstance(mockEncrypterDecrypter)
    )
    .disable[HmrcModule]
    .build()

  lazy val sut: AppConfig = app.injector.instanceOf[AppConfig]

  "applePass" must {

    "read the configured certificate values" in {
      val certs = sut.appleCerts.futureValue
      certs.wwdrca mustBe "appleWWDRCA"
      certs.privateCert mustBe "privateCertificate"
      certs.privateCertPassword mustBe "privateCertificatePassword"
    }

    "read signingEnabled flag" in {
      sut.applePassSigningEnabled mustBe false
    }
  }
}
