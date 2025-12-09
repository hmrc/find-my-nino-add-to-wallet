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

import cats.data.EitherT
import config.AppConfig
import models.apple.ApplePassCard
import play.api.Logging
import repositories.ApplePassRepoTrait

import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.util.UUID
import java.util.zip.{ZipEntry, ZipOutputStream}
import javax.inject._
import scala.concurrent.{ExecutionContext, Future}

trait ApplePassService {

  def getPassCardByPassIdAndNINO(
    passId: String,
    nino: String
  )(implicit ec: ExecutionContext): Future[Option[Array[Byte]]]

  def getQrCodeByPassIdAndNINO(
    passId: String,
    nino: String
  )(implicit ec: ExecutionContext): Future[Option[Array[Byte]]]

  def createPass(name: String, nino: String)(implicit ec: ExecutionContext): EitherT[Future, Exception, String]
}

@Singleton
class StubApplePassService @Inject() (
  qrCodeService: QrCodeService
) extends ApplePassService
    with Logging {

  override def getPassCardByPassIdAndNINO(
    passId: String,
    nino: String
  )(implicit ec: ExecutionContext): Future[Option[Array[Byte]]] = {
    logger.warn(s"[StubApplePassService] getPassCardByPassIdAndNINO stubbed for passId=$passId, nino=$nino")

    val passJson =
      """{
        |  "formatVersion": 1,
        |  "passTypeIdentifier": "pass.uk.gov.hmrc.sca.nino",
        |  "teamIdentifier": "4LL5YKZZU7",
        |  "organizationName": "HMRC",
        |  "serialNumber": "cf6038e0-2713-4710-8d6c-2a80d78803cf",
        |  "description": "National Insurance number",
        |  "logoText": "HM Revenue & Customs",
        |  "foregroundColor": "rgb(255, 255, 255)",
        |  "backgroundColor": "rgb(0, 137, 133)",
        |  "labelColor": "rgb(255, 255, 255)",
        |  "sharingProhibited": true,
        |  "generic": {
        |    "primaryFields": [
        |      {
        |        "key": "nino",
        |        "label": "NATIONAL INSURANCE NUMBER",
        |        "value": "AA 00 00 03 B",
        |        "textAlignment": "PKTextAlignmentLeft"
        |      }
        |    ],
        |    "secondaryFields": [
        |      {
        |        "key": "name",
        |        "label": "NAME",
        |        "value": "Mrs BOB JONE'S",
        |        "textAlignment": "PKTextAlignmentLeft"
        |      }
        |    ],
        |    "auxiliaryFields": [
        |      {
        |        "key": "warning",
        |        "value": "This is not proof of your identity or your right to work in the UK.",
        |        "textAlignment": "PKTextAlignmentLeft"
        |      }
        |    ],
        |    "backFields": [
        |      {
        |        "key": "downloadpasspdf",
        |        "label": "Your National Insurance number on a letter",
        |        "value": "You can get a letter confirming your National Insurance number from your personal tax account.\nTo sign in, you’ll need to create or use an existing Government Gateway user ID and password.\nhttps://www.tax.service.gov.uk/gg/sign-in?continue=/personal-account/national-insurance-summary/print-letter",
        |        "textAlignment": "PKTextAlignmentLeft"
        |      },
        |      {
        |        "key": "warning",
        |        "value": "To help prevent identity fraud, only share your number when necessary.",
        |        "textAlignment": "PKTextAlignmentLeft"
        |      },
        |      {
        |        "key": "steps",
        |        "label": "You'll need it when you:",
        |        "value": "• start paid work\n• apply for a driving licence\n• apply for a student loan\n• register to vote\n• claim state benefits",
        |        "textAlignment": "PKTextAlignmentLeft"
        |      },
        |      {
        |        "key": "info",
        |        "label": "Your National Insurance number is:",
        |        "value": "• unique to you and never changes\n• not proof of your identity\n• not proof of your right to work in the UK",
        |        "textAlignment": "PKTextAlignmentLeft"
        |      },
        |      {
        |        "key": "findoutmore",
        |        "label": "Find out more about National Insurance",
        |        "value": "https://www.gov.uk/national-insurance",
        |        "textAlignment": "PKTextAlignmentLeft"
        |      }
        |    ]
        |  }
        |}""".stripMargin

    val baos = new ByteArrayOutputStream()
    val zos  = new ZipOutputStream(baos)

    val entry = new ZipEntry("pass.json")
    zos.putNextEntry(entry)
    zos.write(passJson.getBytes(StandardCharsets.UTF_8))
    zos.closeEntry()
    zos.close()

    val applePassCardBytes = baos.toByteArray

    Future.successful(Some(applePassCardBytes))
  }

  override def getQrCodeByPassIdAndNINO(
    passId: String,
    nino: String
  )(implicit ec: ExecutionContext): Future[Option[Array[Byte]]] = {
    logger.warn(s"[StubApplePassService] getQrCodeByPassIdAndNINO stubbed for passId=$passId, nino=$nino")
    Future.successful(qrCodeService.createQRCode("/foo/bar"))
  }

  override def createPass(
    name: String,
    nino: String
  )(implicit ec: ExecutionContext): EitherT[Future, Exception, String] = {
    val uuid = UUID.randomUUID().toString
    logger.warn(s"[StubApplePassService] createPass stubbed for name=$name, nino=$nino, uuid=$uuid")
    EitherT(Future.successful(Right(uuid)))
  }
}

@Singleton
class RealApplePassService @Inject() (
  val config: AppConfig,
  val applePassRepository: ApplePassRepoTrait,
  val fileService: FileService,
  val signatureService: SignatureService,
  val qrCodeService: QrCodeService
) extends ApplePassService
    with Logging {

  override def getPassCardByPassIdAndNINO(passId: String, nino: String)(implicit
    ec: ExecutionContext
  ): Future[Option[Array[Byte]]] =
    for {
      ap <- applePassRepository.findByPassId(passId)
    } yield ap match {
      case Some(applePass) =>
        if (applePass.nino.replace(" ", "").take(8).equals(nino.take(8))) {
          Some(applePass.applePassCard)
        } else {
          logger.warn("Pass NINO does not match session NINO")
          None
        }
      case _               => None
    }

  override def getQrCodeByPassIdAndNINO(passId: String, nino: String)(implicit
    ec: ExecutionContext
  ): Future[Option[Array[Byte]]] =
    for {
      aQrCode <- applePassRepository.findByPassId(passId)
    } yield aQrCode match {
      case Some(applePass) =>
        if (applePass.nino.replace(" ", "").take(8).equals(nino.take(8))) {
          Some(applePass.qrCode)
        } else {
          logger.warn("Pass NINO does not match session NINO")
          None
        }
      case _               => None
    }

  override def createPass(name: String, nino: String)(implicit
    ec: ExecutionContext
  ): EitherT[Future, Exception, String] =
    EitherT {
      val uuid = UUID.randomUUID().toString
      val pass = ApplePassCard(name, nino, uuid)

      val passFilesInBytes = fileService.createFileBytesForPass(pass)

      for {
        privateCertificate         <- config.privateCertificate
        privateCertificatePassword <- config.privateCertificatePassword
        appleWWDRCA                <- config.appleWWDRCA
      } yield {
        val signaturePassInBytes = signatureService.createSignatureForPass(
          passFilesInBytes,
          privateCertificate,
          privateCertificatePassword,
          appleWWDRCA
        )

        if (passFilesInBytes.nonEmpty && signaturePassInBytes.content.nonEmpty) {
          val passDataTuple = for {
            pkPassByteArray <- fileService.createPkPassZipForPass(passFilesInBytes, signaturePassInBytes)
            qrCodeByteArray <-
              qrCodeService.createQRCode(s"${config.frontendServiceUrl}/get-pass-card?passId=$uuid&qr-code=true")
          } yield (pkPassByteArray, qrCodeByteArray)
          passDataTuple.map(tuple => applePassRepository.insert(uuid, name, nino, tuple._1, tuple._2))
          Right(uuid)

        } else {
          logger.error(
            s"[Creating Apple Pass] Zip and Qr Code Failed. " +
              s"isPassFilesGenerated: ${passFilesInBytes.nonEmpty} || isPassSigned: ${signaturePassInBytes.content.nonEmpty}"
          )
          Left(
            new Exception(
              s"Problem occurred while creating Apple Pass. "
                + s"Pass files generated: ${passFilesInBytes.nonEmpty}, Pass files signed: ${signaturePassInBytes.content.nonEmpty}"
            )
          )
        }
      }
    }
}

object ApplePassService {
  val KEY_NINO   = "nino"
  val LABEL_NINO = "NATIONAL INSURANCE NUMBER"

  val KEY_NAME   = "name"
  val LABEL_NAME = "NAME"

  val KEY_WARNING  = "warning"
  val TEXT_WARNING = "This is not proof of your identity or your right to work in the UK."
}
