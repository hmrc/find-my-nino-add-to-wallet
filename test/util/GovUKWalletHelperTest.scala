/*
 * Copyright 2023 HM Revenue & Customs
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

package util

import config.AppConfig
import org.mockito.MockitoSugar._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class GovUKWalletHelperSpec extends AnyWordSpec with Matchers {

  "GovUKWalletHelper" should {

    "create GovUKVCDocument" in {
      // Mock the config dependency
      val mockConfig = mock[AppConfig]
      when(mockConfig.govukPassSub).thenReturn("sub")
      when(mockConfig.govukPassNbf).thenReturn(1670336441)
      when(mockConfig.govukPassIss).thenReturn("iss")
      when(mockConfig.govukPassExp).thenReturn(1670336441)
      when(mockConfig.govukPassIat).thenReturn(1670336441)


      val govUKWalletHelper = new GovUKWalletHelper(mockConfig)

      val govUKVCDocument = govUKWalletHelper.createGovUKVCDocument(List("John"), "Doe", "123456789")

      govUKVCDocument.sub shouldBe "sub"
      govUKVCDocument.nbf shouldBe 1670336441
      govUKVCDocument.iss shouldBe "iss"
      govUKVCDocument.exp shouldBe 1670336441
      govUKVCDocument.iat shouldBe 1670336441

      govUKVCDocument.vc.`type` shouldBe List("VerifiableCredential", "SocialSecurityCredential")


      verify(mockConfig, atLeastOnce).govukPassSub
      verify(mockConfig, atLeastOnce).govukPassNbf
      verify(mockConfig, atLeastOnce).govukPassIss
      verify(mockConfig, atLeastOnce).govukPassExp
      verify(mockConfig, atLeastOnce).govukPassIat
    }

    "create and sign JWT" in {

      val dummyKey = "MEECAQAwEwYHKoZIzj0CAQYIKoZIzj0DAQcEJzAlAgEBBCDQc+R+wHL04DWmQMeLaU4Tz/AuNJzaownauQlNbtawzw=="

      val mockConfig = mock[AppConfig]
      when(mockConfig.govukPassSub).thenReturn("sub")
      when(mockConfig.govukPassNbf).thenReturn(1670336441)
      when(mockConfig.govukPassIss).thenReturn("iss")
      when(mockConfig.govukPassExp).thenReturn(1670336441)
      when(mockConfig.govukPassIat).thenReturn(1670336441)
      when(mockConfig.govukVerificatonPrivateKey).thenReturn(dummyKey)

      val govUKWalletHelper = new GovUKWalletHelper(mockConfig)

      val govUKVCDocument = govUKWalletHelper.createGovUKVCDocument(List("John"), "Doe", "123456789")

      val jwt = govUKWalletHelper.createAndSignJWT(govUKVCDocument)

      verify(mockConfig, atLeastOnce).govukPassSub
      verify(mockConfig, atLeastOnce).govukPassNbf
      verify(mockConfig, atLeastOnce).govukPassIss
      verify(mockConfig, atLeastOnce).govukPassExp
      verify(mockConfig, atLeastOnce).govukPassIat
      verify(mockConfig, atLeastOnce).govukVerificatonPrivateKey
    }
  }
}
