/*
 * Copyright 2024 HM Revenue & Customs
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
import models.{Name, NameParts}
import org.mockito.MockitoSugar._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.Json

class GovUKWalletHelperSpec extends AnyWordSpec with Matchers {

  "GovUKWalletHelper" should {

    "create GovUKVCDocument" in {
      // Mock the config dependency
      val mockConfig = mock[AppConfig]
      when(mockConfig.govukPassContext).thenReturn(List("https://www.w3.org/2018/credentials/v1"))
      when(mockConfig.govukPassSub).thenReturn("sub")
      when(mockConfig.govukPassNbf).thenReturn(1670336441)
      when(mockConfig.govukPassIss).thenReturn("iss")
      when(mockConfig.govukPassExp).thenReturn(1670336441)
      when(mockConfig.govukPassIat).thenReturn(1670336441)

      val govUKWalletHelper = new GovUKWalletHelper(mockConfig)

      val govUKVCDocument = govUKWalletHelper.createGovUKVCDocument("Mr", "John", "Doe", "123456789")

      govUKVCDocument.`@context` shouldBe List("https://www.w3.org/2018/credentials/v1")
      govUKVCDocument.sub shouldBe "sub"
      govUKVCDocument.nbf.getClass shouldBe classOf[Int]
      govUKVCDocument.iss shouldBe "iss"
      govUKVCDocument.exp.getClass shouldBe classOf[Int]
      govUKVCDocument.iat.getClass shouldBe classOf[Int]

      govUKVCDocument.vc.`type` shouldBe List("VerifiableCredential", "SocialSecurityCredential")

      verify(mockConfig, atLeastOnce).govukPassContext

    }

    "create and sign JWT" in {

      val dummyKey = "MEECAQAwEwYHKoZIzj0CAQYIKoZIzj0DAQcEJzAlAgEBBCDQc+R+wHL04DWmQMeLaU4Tz/AuNJzaownauQlNbtawzw=="

      val mockConfig = mock[AppConfig]
      when(mockConfig.govukPassContext).thenReturn(List("https://www.w3.org/2018/credentials/v1"))
      when(mockConfig.govukPassSub).thenReturn("sub")
      when(mockConfig.govukPassNbf).thenReturn(1670336441)
      when(mockConfig.govukPassIss).thenReturn("iss")
      when(mockConfig.govukPassExp).thenReturn(1670336441)
      when(mockConfig.govukPassIat).thenReturn(1670336441)
      when(mockConfig.govukVerificatonPrivateKey).thenReturn(dummyKey)

      val govUKWalletHelper = new GovUKWalletHelper(mockConfig)

      val govUKVCDocument = govUKWalletHelper.createGovUKVCDocument("Mr","John", "Doe", "123456789")

      val jwt = govUKWalletHelper.createAndSignJWT(govUKVCDocument)

      verify(mockConfig, atLeastOnce).govukPassContext
      verify(mockConfig, atLeastOnce).govukPassSub
      verify(mockConfig, atLeastOnce).govukPassIss
      verify(mockConfig, atLeastOnce).govukVerificatonPrivateKey

      govUKVCDocument.vc.credentialSubject.name shouldEqual (List(
        Name(List(
          NameParts("Title","Mr"),
          NameParts("GivenName","John"),
          NameParts("FamilyName","Doe")
        ))
      ))
    }
  }
}
