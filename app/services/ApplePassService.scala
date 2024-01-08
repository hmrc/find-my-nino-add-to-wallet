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
import models.{ApplePassCard, ApplePassDetails}
import play.api.Logging
import repositories.ApplePassRepository
import cats.syntax.all._

import java.nio.file.{Files, Path}
import java.util.UUID
import javax.inject._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class ApplePassService @Inject()(val config: AppConfig,
                                 val applePassRepository: ApplePassRepository,
                                 val fileService: FileService,
                                 val signatureService: SignatureService,
                                 val qrCodeService: QrCodeService) extends Logging {


  def getPassCardByPassIdAndNINO(passId: String, nino: String)(implicit ec: ExecutionContext): Future[Option[Array[Byte]]] = {
    for {
      ap <- applePassRepository.findByPassId(passId)
    } yield {
      ap match {
        case Some(applePass) => {
          if (applePass.nino.replace(" ", "").equals(nino)) {
            Some(applePass.applePassCard)
          } else {
            logger.warn("Pass NINO does not match session NINO")
            None
          }
        }
        case _ => None
      }
    }
  }


  def getQrCodeByPassIdAndNINO(passId: String, nino: String)(implicit ec: ExecutionContext): Future[Option[Array[Byte]]] = {
    for {
      aQrCode <- applePassRepository.findByPassId(passId)
    } yield {
      aQrCode match {
        case Some(applePass) => {
          if (applePass.nino.replace(" ","").equals(nino)) {
            Some(applePass.qrCode)
          } else {
            logger.info("Pass NINO does not match session NINO")
            None
          }
        }
        case _ => None
      }
    }
  }

  def getPassDetails(passId: String, nino: String)(implicit ec: ExecutionContext): Future[Option[ApplePassDetails]] = {
    for {
      ap <- applePassRepository.findByPassId(passId)
    } yield {
      ap match {
        case Some(applePass) => {
          if (applePass.nino.replace(" ","").equals(nino)) {
            Some(ApplePassDetails(applePass.fullName, applePass.nino))
          } else {
            logger.warn("Pass NINO does not match session NINO")
            None
          }
        }
        case _ => None
      }
    }
  }

  def getPassDetailsWithNameAndNino(fullName: String, nino: String)(implicit ec: ExecutionContext): Future[Option[ApplePassDetails]] = {
    applePassRepository.findByNameAndNino(fullName, nino).map(_.map(r => ApplePassDetails(r.fullName, r.nino)))
  }

  def createPass(name: String, nino: String)(implicit ec: ExecutionContext): Either[Exception, String] = {

    val uuid = UUID.randomUUID().toString
    val path = Files.createTempDirectory(s"$uuid.pass").toAbsolutePath
    val enPath = Path.of(s"$path", "en.lproj")
    val cyPath = Path.of(s"$path", "cy.lproj")

    logger.info(s"This is the en path ${enPath.toString}")
    logger.info(s"This is the path ${path.toString}")

    val pass = ApplePassCard(name, nino, uuid)
    logger.info(s"[Creating Apple Pass] creating dir")
    val isDirectoryCreated = fileService.createDirectoryForPass(path, pass, enPath, cyPath)
    logger.info(s"[Creating Apple Pass] isDirectoryCreated: $isDirectoryCreated")

    val isPassSigned = signatureService.createSignatureForPass(path, config.privateCertificate, config.privateCertificatePassword, config.appleWWDRCA)
    logger.info(s"[Creating Apple Pass] isPassSigned: $isPassSigned")

    if (isDirectoryCreated && isPassSigned) {
      val passDataTuple = for {
        pkPassByteArray <- fileService.createPkPassZipForPass(path)
        qrCodeByteArray <- qrCodeService.createQRCode(s"${config.frontendServiceUrl}/get-pass-card?passId=$uuid&qr-code=true")
      } yield (pkPassByteArray, qrCodeByteArray)

      logger.info(s"[Creating Apple Pass] Zip and Qr Code Completed")
      val deleteDir = fileService.deleteDirectory(path)
      logger.info(s"Was directory deleted: $deleteDir")
      passDataTuple match {
        case Some(tuple) =>
          applePassRepository.insert(uuid, name, nino, tuple._1, tuple._2)
        case None => Future.successful(logger.info("No pk pass or qr code data"))
      }
      logger.info(s"[Creating Apple Pass] Insert Apple pass to DB Completed")
      Right(uuid)
    } else {
      logger.error(s"[Creating Apple Pass] Zip and Qr Code Failed. " +
        s"isDirectoryCreated: $isDirectoryCreated " +
        s"|| isPassSigned: $isPassSigned"
      )
      fileService.deleteDirectory(path) // It should delete the pass directory even there is a mistake
      Left(new Exception(s"Problem occurred while creating Apple Pass. " +
        s"Directory created: $isDirectoryCreated, pass signed: $isPassSigned"))
    }
  }
}

object ApplePassService {
  val KEY_NINO = "nino"
  val LABEL_NINO = "NationalInsuranceNumber"

  val KEY_NAME = "name"
  val LABEL_NAME = "NAME"

  val KEY_WARNING = "warning"
  val LABEL_WARNING = "WarningLabel"
  val TEXT_WARNING = "WarningText"
}