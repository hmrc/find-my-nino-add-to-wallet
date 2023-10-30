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

package services

import config.AppConfig
import org.mockito.MockitoSugar
import org.mockito.ArgumentMatchers.any
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import repositories.GovUKPassRepository
import util.GovUKWalletHelper

import scala.concurrent.ExecutionContext.Implicits.global

class GovUKPassServiceSpec extends AsyncWordSpec with Matchers with MockitoSugar with BeforeAndAfterEach {
  val passId: String = "test-pass-id-001"

  private val mockGovUKPassRepository = mock[GovUKPassRepository]
  private val mockQrCodeService = mock[QrCodeService]
  private val mockAppConfig = mock[AppConfig]
  private val mockGovUKWalletHelper = mock[GovUKWalletHelper]


  private val govUKPassService = new GovUKPassService(
    mockAppConfig,
    mockGovUKPassRepository,
    mockQrCodeService,
    mockGovUKWalletHelper)

  "createGovUKPass" must {
    "return an uuid when success" in {
      when(mockQrCodeService.createQRCode(any(), any()))
        .thenReturn(Some("SomeQrCode".getBytes()))
      val nino = "AB 12 34 56 Q"
      val givenNames = List("TestGivenName1", "TestGivenName2")
      val familyName = "TestSurname"

      val eitherResult = govUKPassService.createGovUKPass(givenNames, familyName, nino)(global)
      eitherResult.isLeft mustBe false
    }
  }

}