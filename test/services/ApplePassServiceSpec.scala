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

package services

import config.AppConfig
import models.apple.ApplePass
import org.mockito.ArgumentMatchers.{any, anyByte, anyList, anyMap, anyString, eq => eqTo}
import org.mockito.MockitoSugar
import org.mockito.MockitoSugar.mock
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import repositories.ApplePassRepository

import java.time.Instant
import scala.concurrent.Future

class ApplePassServiceSpec extends AsyncWordSpec with Matchers with MockitoSugar with BeforeAndAfterEach {

  import ApplePassServiceSpec._

  override def beforeEach(): Unit = {
    reset(mockApplePassRepository, mockFileService, mockSignatureService, mockQrCodeService, mockAppConfig)
  }

  "findQrCodeByPassId" must {
    "return the QR Code when pass id exist" in {
      val qrCode = "QRCodeData".getBytes()
      val applePassCard = "ApplePassCard".getBytes()
      val pass = new ApplePass(passId,
        "Test Name",
        "AB 12 34 56 Q",
        applePassCard,
        qrCode,
        Instant.now()
      )
      when(mockApplePassRepository.findByPassId(eqTo(passId))(any()))
        .thenReturn(Future.successful(Option(pass)))

      applePassService.getQrCodeByPassIdAndNINO(passId,"AB123456Q")(implicitly).map { result =>
        result mustBe Some(qrCode)
      }
    }

    "return None when pass id NOT exist" in {
      when(mockApplePassRepository.findByPassId(eqTo(passId))(any()))
        .thenReturn(Future.successful(None))

      applePassService.getQrCodeByPassIdAndNINO(passId,"AB123456Q")(implicitly).map { result =>
        result mustBe None
      }
    }
  }

  "findApplePassByPassId" must {
    "return the Apple Pass when pass id exist" in {
      val qrCode = "QRCodeData".getBytes()
      val applePassCard = "ApplePassCard".getBytes()
      val pass = new ApplePass(
        passId,
        "Test Name",
        "AB 12 34 56 Q",
        applePassCard,
        qrCode,
        Instant.now()
      )
      when(mockApplePassRepository.findByPassId(eqTo(passId))(any()))
        .thenReturn(Future.successful(Option(pass)))

      applePassService.getPassCardByPassIdAndNINO(passId,"AB123456Q")(implicitly).map { result =>
        result mustBe Some(applePassCard)
      }
    }

    "return Apple Pass when pass id NOT exist" in {
      when(mockApplePassRepository.findByPassId(eqTo(passId))(any()))
        .thenReturn(Future.successful(None))

      applePassService.getPassCardByPassIdAndNINO(passId,"AB123456Q")(implicitly).map { result =>
        result mustBe None
      }
    }
  }

  "createPass" must {

    val passFilesGenerated = List(FileAsBytes("", Array.emptyByteArray))
    val blankFileAsBytes = FileAsBytes("", Array.emptyByteArray)

    "should not return an uuid when 'Create File in Bytes for Pass' has failed" in {
      when(mockFileService.createFileBytesForPass(any()))
        .thenReturn(List.empty)

      when(mockSignatureService.createSignatureForPass(any(), any(), any(), any()))
        .thenReturn(blankFileAsBytes)

      val eitherResult = applePassService.createPass("TestName TestSurname",
        "AB 12 34 56 Q")
      eitherResult.isRight mustBe false
      eitherResult match {
        case Left(exception: Exception) =>
          verify(mockFileService, never).createPkPassZipForPass(any(), any())
          verify(mockQrCodeService, never).createQRCode(any(), any())
          verify(mockApplePassRepository, never).insert(anyString(), eqTo("TestName TestSurname"), eqTo("AB 12 34 56 Q"), any(), any()) (any())
          exception.getMessage mustBe "Problem occurred while creating Apple Pass. Pass files generated: false, Pass files signed: false"
      }
    }

    "should not return an uuid when 'Create Signature' failed" in {
      when(mockFileService.createFileBytesForPass(any()))
        .thenReturn(passFilesGenerated)

      when(mockSignatureService.createSignatureForPass(any(), any(), any(), any()))
        .thenReturn(blankFileAsBytes)

      val eitherResult = applePassService.createPass("TestName TestSurname",
        "AB 12 34 56 Q"
      )
      eitherResult.isRight mustBe false
      eitherResult match {
        case Left(exception: Exception) =>
          verify(mockFileService, never).createPkPassZipForPass(any(), any())
          verify(mockQrCodeService, never).createQRCode(any(), any())
          verify(mockApplePassRepository, never).insert(anyString(), eqTo("TestName TestSurname"), eqTo("AB 12 34 56 Q"), any(), any()) (any())
          exception.getMessage mustBe "Problem occurred while creating Apple Pass. Pass files generated: true, Pass files signed: false"
      }
    }

    "return an uuid when success" in {
      when(mockFileService.createFileBytesForPass(any()))
        .thenReturn(passFilesGenerated)

      when(mockSignatureService.createSignatureForPass(any(), any(), any(), any()))
        .thenReturn(FileAsBytes("test", "test".getBytes()))

      when(mockQrCodeService.createQRCode(any(), any()))
        .thenReturn(Some("SomeQrCode".getBytes()))

      when(mockFileService.createPkPassZipForPass(any(), any()))
        .thenReturn(Some("SomeZipFile".getBytes()))

      val eitherResult = applePassService.createPass(
        "TestName TestSurname",
        "AB 12 34 56 Q"
      )

      eitherResult.isLeft mustBe false
      eitherResult match {
        case Right(uuid) =>
          verify(mockFileService, times(1)).createPkPassZipForPass(any(), any())
          verify(mockQrCodeService, times(1)).createQRCode(any(), any())
          verify(mockApplePassRepository, times(1)).insert(anyString(), eqTo("TestName TestSurname"), eqTo("AB 12 34 56 Q"), any(), any())(any())
          uuid.length mustBe 36
      }
    }
  }
}

object ApplePassServiceSpec {
  val passId: String = "test-pass-id-001"
  val SIGNATURE_FILE_NAME = "signature"

  private val mockApplePassRepository = mock[ApplePassRepository]
  private val mockFileService = mock[FileService]
  private val mockSignatureService = mock[SignatureService]
  private val mockQrCodeService = mock[QrCodeService]
  private val mockAppConfig = mock[AppConfig]

  val applePassService = new ApplePassService(mockAppConfig, mockApplePassRepository, mockFileService, mockSignatureService, mockQrCodeService)
}
