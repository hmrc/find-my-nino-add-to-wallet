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

import config.AppConfig
import models.apple.ApplePass
import org.mockito.ArgumentMatchers.{any, anyString, eq as eqTo}
import org.mockito.Mockito.*
import org.scalatestplus.mockito.MockitoSugar.mock
import repositories.ApplePassRepository
import util.SpecBase

import java.time.Instant
import scala.concurrent.Future

class ApplePassServiceSpec extends SpecBase {

  import ApplePassServiceSpec.*

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockApplePassRepository, mockFileService, mockSignatureService, mockQrCodeService, mockAppConfig)
  }

  "findQrCodeByPassId" must {
    "return the QR Code when pass id exist" in {
      val qrCode        = "QRCodeData".getBytes()
      val applePassCard = "ApplePassCard".getBytes()
      val pass          = new ApplePass(passId, "Test Name", "AB 12 34 56 Q", applePassCard, qrCode, Instant.now())

      when(mockApplePassRepository.findByPassId(eqTo(passId))(any()))
        .thenReturn(Future.successful(Some(pass)))

      applePassService(signingEnabled = true)
        .getQrCodeByPassIdAndNINO(passId, "AB123456Q")(implicitly)
        .map { result =>
          result mustBe Some(qrCode)
        }
    }

    "return None when pass id NOT exist" in {
      when(mockApplePassRepository.findByPassId(eqTo(passId))(any()))
        .thenReturn(Future.successful(None))

      applePassService(signingEnabled = true)
        .getQrCodeByPassIdAndNINO(passId, "AB123456Q")(implicitly)
        .map { result =>
          result mustBe None
        }
    }
  }

  "findApplePassByPassId" must {
    "return the Apple Pass when pass id exist" in {
      val qrCode        = "QRCodeData".getBytes()
      val applePassCard = "ApplePassCard".getBytes()
      val pass          = new ApplePass(passId, "Test Name", "AB 12 34 56 Q", applePassCard, qrCode, Instant.now())

      when(mockApplePassRepository.findByPassId(eqTo(passId))(any()))
        .thenReturn(Future.successful(Some(pass)))

      applePassService(signingEnabled = true)
        .getPassCardByPassIdAndNINO(passId, "AB123456Q")(implicitly)
        .map { result =>
          result mustBe Some(applePassCard)
        }
    }

    "return None when pass id NOT exist" in {
      when(mockApplePassRepository.findByPassId(eqTo(passId))(any()))
        .thenReturn(Future.successful(None))

      applePassService(signingEnabled = true)
        .getPassCardByPassIdAndNINO(passId, "AB123456Q")(implicitly)
        .map { result =>
          result mustBe None
        }
    }
  }

  "createPass" must {

    val passFilesGenerated = List(FileAsBytes("manifest.json", "manifest".getBytes()))
    val blankSignature     = FileAsBytes(SignatureService.SIGNATURE_FILE_NAME, Array.emptyByteArray)

    "should not return an uuid when 'Create File in Bytes for Pass' has failed" in {
      when(mockFileService.createFileBytesForPass(any()))
        .thenReturn(List.empty)

      val eitherResult =
        applePassService(signingEnabled = true)
          .createPass("TestName TestSurname", "AB 12 34 56 Q")
          .value
          .futureValue

      eitherResult.isRight mustBe false
      eitherResult match {
        case Right(_)                   =>
          fail("Should not return an uuid when 'Create File in Bytes for Pass' has failed")
        case Left(exception: Exception) =>
          verify(mockFileService, never).createPkPassZipForPass(any(), any())
          verify(mockQrCodeService, never).createQRCode(any(), any())
          verify(mockApplePassRepository, never)
            .insert(anyString(), eqTo("TestName TestSurname"), eqTo("AB 12 34 56 Q"), any(), any())(any())
          verify(mockSignatureService, never).createSignatureForPass(any(), any(), any(), any())
          verify(mockAppConfig, never).appleCerts
          exception.getMessage mustBe
            "Problem occurred while creating Apple Pass. Pass files generated: false"
      }
    }

    "should not return an uuid when 'Create Signature' failed (signing enabled)" in {
      when(mockFileService.createFileBytesForPass(any()))
        .thenReturn(passFilesGenerated)

      when(mockAppConfig.appleCerts).thenReturn(
        Future.successful(mockAppConfig.AppleCerts("wwdrca", "p12", "pwd"))
      )

      when(mockSignatureService.createSignatureForPass(any(), any(), any(), any()))
        .thenReturn(blankSignature)

      val eitherResult =
        applePassService(signingEnabled = true)
          .createPass("TestName TestSurname", "AB 12 34 56 Q")
          .value
          .futureValue

      eitherResult.isRight mustBe false
      eitherResult match {
        case Right(_)                   =>
          fail("Should not return an uuid when 'Create Signature' failed")
        case Left(exception: Exception) =>
          verify(mockFileService, never).createPkPassZipForPass(any(), any())
          verify(mockQrCodeService, never).createQRCode(any(), any())
          verify(mockApplePassRepository, never)
            .insert(anyString(), eqTo("TestName TestSurname"), eqTo("AB 12 34 56 Q"), any(), any())(any())
          exception.getMessage mustBe
            "Problem occurred while creating Apple Pass. Pass files generated: true, Pass files signed: false"
      }
    }

    "return an uuid when signing is disabled and signature is empty" in {
      when(mockFileService.createFileBytesForPass(any()))
        .thenReturn(passFilesGenerated)

      when(mockQrCodeService.createQRCode(any(), any()))
        .thenReturn(Some("SomeQrCode".getBytes()))

      when(mockFileService.createPkPassZipForPass(any(), any()))
        .thenReturn(Some("SomeZipFile".getBytes()))

      when(mockApplePassRepository.insert(anyString(), anyString(), anyString(), any(), any())(any()))
        .thenReturn(Future.successful(()))

      val eitherResult =
        applePassService(signingEnabled = false)
          .createPass("TestName TestSurname", "AB 12 34 56 Q")
          .value
          .futureValue

      eitherResult.isLeft mustBe false
      eitherResult match {
        case Right(uuid) =>
          verify(mockAppConfig, never).appleCerts
          verify(mockSignatureService, never).createSignatureForPass(any(), any(), any(), any())
          verify(mockFileService, times(1)).createPkPassZipForPass(any(), any())
          verify(mockQrCodeService, times(1)).createQRCode(any(), any())
          verify(mockApplePassRepository, times(1))
            .insert(anyString(), eqTo("TestName TestSurname"), eqTo("AB 12 34 56 Q"), any(), any())(any())
          uuid.length mustBe 36
        case Left(_)     =>
          fail("Should return an uuid when signing is disabled")
      }
    }

    "return an uuid when success (signing enabled)" in {
      when(mockFileService.createFileBytesForPass(any()))
        .thenReturn(passFilesGenerated)

      when(mockAppConfig.appleCerts).thenReturn(
        Future.successful(mockAppConfig.AppleCerts("wwdrca", "p12", "pwd"))
      )

      when(mockSignatureService.createSignatureForPass(any(), any(), any(), any()))
        .thenReturn(FileAsBytes(SignatureService.SIGNATURE_FILE_NAME, "sig".getBytes()))

      when(mockQrCodeService.createQRCode(any(), any()))
        .thenReturn(Some("SomeQrCode".getBytes()))

      when(mockFileService.createPkPassZipForPass(any(), any()))
        .thenReturn(Some("SomeZipFile".getBytes()))

      when(mockApplePassRepository.insert(anyString(), anyString(), anyString(), any(), any())(any()))
        .thenReturn(Future.successful(()))

      val eitherResult =
        applePassService(signingEnabled = true)
          .createPass("TestName TestSurname", "AB 12 34 56 Q")
          .value
          .futureValue

      eitherResult.isLeft mustBe false
      eitherResult match {
        case Right(uuid) =>
          verify(mockAppConfig, times(1)).appleCerts
          verify(mockSignatureService, times(1)).createSignatureForPass(any(), any(), any(), any())
          verify(mockFileService, times(1)).createPkPassZipForPass(any(), any())
          verify(mockQrCodeService, times(1)).createQRCode(any(), any())
          verify(mockApplePassRepository, times(1))
            .insert(anyString(), eqTo("TestName TestSurname"), eqTo("AB 12 34 56 Q"), any(), any())(any())
          uuid.length mustBe 36
        case Left(_)     =>
          fail("Should return an uuid when success")
      }
    }
  }
}

object ApplePassServiceSpec {
  val passId: String = "test-pass-id-001"

  private val mockApplePassRepository = mock[ApplePassRepository]
  private val mockFileService         = mock[FileService]
  private val mockSignatureService    = mock[SignatureService]
  private val mockQrCodeService       = mock[QrCodeService]
  private val mockAppConfig           = mock[AppConfig]

  private def applePassService(signingEnabled: Boolean): ApplePassService = {
    when(mockAppConfig.applePassSigningEnabled).thenReturn(signingEnabled)
    new ApplePassService(
      mockAppConfig,
      mockApplePassRepository,
      mockFileService,
      mockSignatureService,
      mockQrCodeService
    )
  }
}
