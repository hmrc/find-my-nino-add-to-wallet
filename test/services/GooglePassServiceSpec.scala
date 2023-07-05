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
import models.GooglePassDetails
import org.joda.time.{DateTime, DateTimeZone}
import org.mockito.ArgumentMatchers.{any, anyString, eq => eqTo}
import org.mockito.MockitoSugar
import org.mockito.MockitoSugar.mock
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import repositories.{GooglePass, GooglePassRepository}
import services.googlepass.GooglePassUtil

import scala.concurrent.Future

class GooglePassServiceSpec extends AsyncWordSpec with Matchers with MockitoSugar with BeforeAndAfterEach {

  import GooglePassServiceSpec._

  override def beforeEach(): Unit = {
    reset(mockGooglePassRepository, mockGooglePassUtil, mockQrCodeService, mockAppConfig)
  }

  "getPassDetails" must {
    "return the details when pass id exist" in {
      val qrCode = "QRCodeData".getBytes()
      val googlePassUrl = "https://pay.google.com/gp/v/save/test"
      val pass = new GooglePass(passId,
        "Test Name",
        "AB 12 34 56 Q",
        DateTime.now(DateTimeZone.UTC).plusYears(DEFAULT_EXPIRATION_YEARS).toString(),
        googlePassUrl,
        qrCode,
        DateTime.now()
      )
      when(mockGooglePassRepository.findByPassId(eqTo(passId))(any()))
        .thenReturn(Future.successful(Option(pass)))

      googlePassService.getPassDetails(passId)(implicitly).map { result =>
        result mustBe Some(GooglePassDetails(pass.fullName, pass.nino))
      }
    }

    "return None when pass id NOT exist" in {
      when(mockGooglePassRepository.findByPassId(eqTo(passId))(any()))
        .thenReturn(Future.successful(None))

      googlePassService.getPassDetails(passId)(implicitly).map { result =>
        result mustBe None
      }
    }
  }

  "findQrCodeByPassId" must {
    "return the QR Code when pass id exist" in {
      val qrCode = "QRCodeData".getBytes()
      val googlePassUrl = "https://pay.google.com/gp/v/save/test"
      val pass = new GooglePass(passId,
        "Test Name",
        "AB 12 34 56 Q",
        DateTime.now(DateTimeZone.UTC).plusYears(DEFAULT_EXPIRATION_YEARS).toString(),
        googlePassUrl,
        qrCode,
        DateTime.now()
      )
      when(mockGooglePassRepository.findByPassId(eqTo(passId))(any()))
        .thenReturn(Future.successful(Option(pass)))

      googlePassService.getQrCodeByPassId(passId)(implicitly).map { result =>
        result mustBe Some(qrCode)
      }
    }

    "return None when pass id NOT exist" in {
      when(mockGooglePassRepository.findByPassId(eqTo(passId))(any()))
        .thenReturn(Future.successful(None))

      googlePassService.getQrCodeByPassId(passId)(implicitly).map { result =>
        result mustBe None
      }
    }
  }

  "findGooglePassByPassId" must {
    "return the Google Pass when pass id exist" in {
      val qrCode = "QRCodeData".getBytes()
      val googlePassUrl = "https://pay.google.com/gp/v/save/test"
      val pass = new GooglePass(
        passId,
        "Test Name",
        "AB 12 34 56 Q",
        DateTime.now(DateTimeZone.UTC).plusYears(DEFAULT_EXPIRATION_YEARS).toString(),
        googlePassUrl,
        qrCode,
        DateTime.now()
      )
      when(mockGooglePassRepository.findByPassId(eqTo(passId))(any()))
        .thenReturn(Future.successful(Option(pass)))

      googlePassService.getPassUrlByPassId(passId)(implicitly).map { result =>
        result mustBe Some(googlePassUrl)
      }
    }

    "return Google Pass when pass id NOT exist" in {
      when(mockGooglePassRepository.findByPassId(eqTo(passId))(any()))
        .thenReturn(Future.successful(None))

      googlePassService.getPassUrlByPassId(passId)(implicitly).map { result =>
        result mustBe None
      }
    }
  }

  "createPass" must {

    "return an uuid when success" in {

      when(mockQrCodeService.createQRCode(any(), any()))
        .thenReturn(Some("SomeQrCode".getBytes()))


      val eitherResult = googlePassService.createPass(
        "TestName TestSurname",
        "AB 12 34 56 Q",
        DateTime.now(DateTimeZone.UTC).plusYears(DEFAULT_EXPIRATION_YEARS).toString()
      )

      eitherResult.isLeft mustBe false
      eitherResult match {
        case Right(uuid) =>
          verify(mockQrCodeService, times(1)).createQRCode(any(), any())
          verify(mockGooglePassRepository, times(1)).insert(anyString(), eqTo("TestName TestSurname"), eqTo("AB 12 34 56 Q"), any(), any(), any())(any())
          uuid.length mustBe 36
      }
    }
  }
}

object GooglePassServiceSpec {
  val passId: String = "test-pass-id-001"

  private val mockGooglePassRepository = mock[GooglePassRepository]
  private val mockGooglePassUtil = mock[GooglePassUtil]
  private val mockQrCodeService = mock[QrCodeService]
  private val mockAppConfig = mock[AppConfig]
  private val DEFAULT_EXPIRATION_YEARS = 100

  val googlePassService = new GooglePassService(mockAppConfig, mockGooglePassUtil, mockGooglePassRepository, mockQrCodeService)
}
