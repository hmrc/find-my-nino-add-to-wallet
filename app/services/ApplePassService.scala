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
import models.apple.ApplePassCard
import play.api.Logging
import repositories.ApplePassRepoTrait

import java.util.UUID
import javax.inject._
import scala.concurrent.{ExecutionContext, Future}

class ApplePassService @Inject()(val config: AppConfig,
                                 val applePassRepository: ApplePassRepoTrait,
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
          if (applePass.nino.replace(" ", "").equals(nino)) {
            Some(applePass.qrCode)
          } else {
            logger.warn("Pass NINO does not match session NINO")
            None
          }
        }
        case _ => None
      }
    }
  }

  def createPass(name: String, nino: String)(implicit ec: ExecutionContext): Either[Exception, String] = {
    val uuid = UUID.randomUUID().toString
    val pass = ApplePassCard(name, nino, uuid)

    val passFilesInBytes = fileService.createFileBytesForPass(pass)
    logger.info(s"[Creating Apple Pass] isPassFilesGenerated: ${passFilesInBytes.nonEmpty}")

    val signaturePassInBytes = signatureService.createSignatureForPass(
      passFilesInBytes, config.privateCertificate, config.privateCertificatePassword, config.appleWWDRCA)
    logger.info(s"[Creating Apple Pass] isPassFilesSigned: ${signaturePassInBytes.content.nonEmpty}")

    if (passFilesInBytes.nonEmpty && signaturePassInBytes.content.nonEmpty) {
      val passDataTuple = for {
        pkPassByteArray <- fileService.createPkPassZipForPass(passFilesInBytes, signaturePassInBytes)
        qrCodeByteArray <- qrCodeService.createQRCode(s"${config.frontendServiceUrl}/get-pass-card?passId=$uuid&qr-code=true")
      } yield (pkPassByteArray, qrCodeByteArray)

      logger.info(s"[Creating Apple Pass] Zip and Qr Code Completed")
      passDataTuple.map(tuple => applePassRepository.insert(uuid, name, nino, tuple._1, tuple._2))
      logger.info(s"[Creating Apple Pass] Insert Apple pass to DB Completed")
      Right(uuid)
    } else {
      logger.error(s"[Creating Apple Pass] Zip and Qr Code Failed. " +
        s"isPassFilesGenerated: ${passFilesInBytes.nonEmpty} || isPassSigned: ${signaturePassInBytes.content.nonEmpty}"
      )
      Left(new Exception(s"Problem occurred while creating Apple Pass. "
        + s"Pass files generated: ${passFilesInBytes.nonEmpty}, Pass files signed: ${signaturePassInBytes.content.nonEmpty}"))
    }
  }
}

object ApplePassService {
  val KEY_NINO = "nino"
  val LABEL_NINO = "NATIONAL INSURANCE NUMBER"

  val KEY_NAME = "name"
  val LABEL_NAME = "NAME"

  val KEY_WARNING = "warning"
  val TEXT_WARNING = "This is not proof of your identity or your right to work in the UK."
}