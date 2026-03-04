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

package services

import com.google.auth.oauth2.GoogleCredentials
import config.AppConfig
import models.google.GooglePass
import org.mockito.ArgumentMatchers.{any, anyString, eq as eqTo}
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.mockito.MockitoSugar.mock
import repositories.GooglePassRepository
import services.googlepass.GooglePassUtil

import java.time.{Instant, ZoneId, ZonedDateTime}
import scala.concurrent.Future

class GooglePassServiceSpec extends AsyncWordSpec with Matchers with MockitoSugar with BeforeAndAfterEach {

  import GooglePassServiceSpec.*

  override def beforeEach(): Unit =
    reset(mockGooglePassRepository, mockGooglePassUtil, mockQrCodeService, mockAppConfig)

  "findQrCodeByPassId" must {
    "return the QR Code when pass id exist" in {
      val qrCode        = "QRCodeData".getBytes()
      val googlePassUrl = "https://pay.google.com/gp/v/save/test"
      val pass          = new GooglePass(
        passId,
        "Test Name",
        "AB 12 34 56 Q",
        ZonedDateTime.now(ZoneId.of("UTC")).plusYears(DEFAULT_EXPIRATION_YEARS).toString,
        googlePassUrl,
        qrCode,
        Instant.now()
      )

      when(mockGooglePassRepository.findByPassId(eqTo(passId))(any()))
        .thenReturn(Future.successful(Some(pass)))

      googlePassService.getQrCodeByPassIdAndNINO(passId, "AB123456Q")(implicitly).map { result =>
        result mustBe Some(qrCode)
      }
    }

    "return None when session NINO does not match pass NINO" in {
      val qrCode        = "QRCodeData".getBytes()
      val googlePassUrl = "https://pay.google.com/gp/v/save/test"
      val pass          = new GooglePass(
        passId,
        "Test Name",
        "AB 12 34 56 Q",
        ZonedDateTime.now(ZoneId.of("UTC")).plusYears(DEFAULT_EXPIRATION_YEARS).toString,
        googlePassUrl,
        qrCode,
        Instant.now()
      )

      when(mockGooglePassRepository.findByPassId(eqTo(passId))(any()))
        .thenReturn(Future.successful(Some(pass)))

      googlePassService.getQrCodeByPassIdAndNINO(passId, "AC123456Q")(implicitly).map { result =>
        result mustBe None
      }
    }

    "return None when pass id NOT exist" in {
      when(mockGooglePassRepository.findByPassId(eqTo(passId))(any()))
        .thenReturn(Future.successful(None))

      googlePassService.getQrCodeByPassIdAndNINO(passId, "AB123456Q")(implicitly).map { result =>
        result mustBe None
      }
    }
  }

  "findGooglePassByPassId" must {
    "return the Google Pass URL when pass id exist" in {
      val qrCode        = "QRCodeData".getBytes()
      val googlePassUrl = "https://pay.google.com/gp/v/save/test"
      val pass          = new GooglePass(
        passId,
        "Test Name",
        "AB 12 34 56 Q",
        ZonedDateTime.now(ZoneId.of("UTC")).plusYears(DEFAULT_EXPIRATION_YEARS).toString,
        googlePassUrl,
        qrCode,
        Instant.now()
      )

      when(mockGooglePassRepository.findByPassId(eqTo(passId))(any()))
        .thenReturn(Future.successful(Some(pass)))

      googlePassService.getPassUrlByPassIdAndNINO(passId, "AB123456Q")(implicitly).map { result =>
        result mustBe Some(googlePassUrl)
      }
    }

    "return None when session NINO does not match pass NINO" in {
      val qrCode        = "QRCodeData".getBytes()
      val googlePassUrl = "https://pay.google.com/gp/v/save/test"
      val pass          = new GooglePass(
        passId,
        "Test Name",
        "AB 12 34 56 Q",
        ZonedDateTime.now(ZoneId.of("UTC")).plusYears(DEFAULT_EXPIRATION_YEARS).toString,
        googlePassUrl,
        qrCode,
        Instant.now()
      )

      when(mockGooglePassRepository.findByPassId(eqTo(passId))(any()))
        .thenReturn(Future.successful(Some(pass)))

      googlePassService.getPassUrlByPassIdAndNINO(passId, "AC123456Q")(implicitly).map { result =>
        result mustBe None
      }
    }

    "return None when pass id NOT exist" in {
      when(mockGooglePassRepository.findByPassId(eqTo(passId))(any()))
        .thenReturn(Future.successful(None))

      googlePassService.getPassUrlByPassIdAndNINO(passId, "AB123456Q")(implicitly).map { result =>
        result mustBe None
      }
    }
  }

  "createPassWithCredentials" must {
    "return an uuid when success" in {
      when(mockGooglePassUtil.createGooglePassWithCredentials(anyString(), anyString(), any()))
        .thenReturn("https://pay.google.com/gp/v/save/test")

      when(mockQrCodeService.createQRCode(any(), any()))
        .thenReturn(Some("SomeQrCode".getBytes()))

      when(
        mockGooglePassRepository.insert(anyString(), anyString(), anyString(), anyString(), anyString(), any())(any())
      )
        .thenReturn(Future.successful(()))

      googlePassService
        .createPassWithCredentials(
          "TestName TestSurname",
          "AB 12 34 56 Q",
          ZonedDateTime.now(ZoneId.of("UTC")).plusYears(DEFAULT_EXPIRATION_YEARS).toString,
          mockGoogleCredentials
        )
        .map {
          case Right(uuid) =>
            verify(mockGooglePassUtil, times(1)).createGooglePassWithCredentials(anyString(), anyString(), any())
            verify(mockQrCodeService, times(1)).createQRCode(any(), any())
            verify(mockGooglePassRepository, times(1))
              .insert(
                anyString(),
                eqTo("TestName TestSurname"),
                eqTo("AB 12 34 56 Q"),
                anyString(),
                anyString(),
                any()
              )(any())
            uuid.length mustBe 36

          case Left(e) =>
            fail(s"Expected Right, got Left(${e.getMessage})")
        }
    }

    "return Left when repository insert fails" in {
      when(mockGooglePassUtil.createGooglePassWithCredentials(anyString(), anyString(), any()))
        .thenReturn("https://pay.google.com/gp/v/save/test")

      when(mockQrCodeService.createQRCode(any(), any()))
        .thenReturn(Some("SomeQrCode".getBytes()))

      when(
        mockGooglePassRepository.insert(anyString(), anyString(), anyString(), anyString(), anyString(), any())(any())
      )
        .thenReturn(Future.failed(new RuntimeException("db down")))

      googlePassService
        .createPassWithCredentials(
          "TestName TestSurname",
          "AB 12 34 56 Q",
          ZonedDateTime.now(ZoneId.of("UTC")).plusYears(DEFAULT_EXPIRATION_YEARS).toString,
          mockGoogleCredentials
        )
        .map {
          case Left(e)  =>
            e.getMessage mustBe "Problem occurred while storing Google Pass."
          case Right(_) =>
            fail("Expected Left")
        }
    }
  }
}

object GooglePassServiceSpec {
  val passId: String = "test-pass-id-001"

  private val mockGooglePassRepository = mock[GooglePassRepository]
  private val mockGooglePassUtil       = mock[GooglePassUtil]
  private val mockQrCodeService        = mock[QrCodeService]
  private val mockAppConfig            = mock[AppConfig]
  private val DEFAULT_EXPIRATION_YEARS = 100
  private val mockGoogleCredentials    = mock[GoogleCredentials]

  val googlePassService =
    new GooglePassService(mockAppConfig, mockGooglePassUtil, mockGooglePassRepository, mockQrCodeService)
}
