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
import models.{ApplePassCard, ApplePassDetails}
import play.api.Logging
import repositories.ApplePassRepository

import java.nio.file.Files
import java.util.UUID
import javax.inject._
import scala.concurrent.{ExecutionContext, Future}

class ApplePassService @Inject()(val config: AppConfig,
                                 val applePassRepository: ApplePassRepository,
                                 val fileService: FileService,
                                 val signatureService: SignatureService,
                                 val qrCodeService: QrCodeService) extends Logging {

  def getPassCardByPassId(passId: String)(implicit ec: ExecutionContext): Future[Option[Array[Byte]]] = {
    applePassRepository.findByPassId(passId).map(_.map(_.applePassCard))
  }

  def getQrCodeByPassId(passId: String)(implicit ec: ExecutionContext): Future[Option[Array[Byte]]] = {
    applePassRepository.findByPassId(passId).map(_.map(_.qrCode))
  }

  def getPassDetails(passId: String)(implicit ec: ExecutionContext): Future[Option[ApplePassDetails]] = {
    applePassRepository.findByPassId(passId).map(_.map(r => ApplePassDetails(r.fullName, r.nino)))
  }

  def getPassDetailsWithNameAndNino(fullName: String, nino: String)(implicit ec: ExecutionContext): Future[Option[ApplePassDetails]] = {
    applePassRepository.findByNameAndNino(fullName, nino).map(_.map(r => ApplePassDetails(r.fullName, r.nino)))
  }

  def createPass(name: String, nino: String)(implicit ec: ExecutionContext): Either[Exception, String] = {

    val uuid = UUID.randomUUID().toString
    val path = Files.createTempDirectory(s"$uuid.pass").toAbsolutePath

    val pass = ApplePassCard(name, nino, uuid)
    val isDirectoryCreated = fileService.createDirectoryForPass(path, pass)
    logger.info(s"[Creating Apple Pass] isDirectoryCreated: $isDirectoryCreated")

    val isPassSigned = signatureService.createSignatureForPass(path, config.privateCertificate, config.privateCertificatePassword, config.appleWWDRCA)
    logger.info(s"[Creating Apple Pass] isPassSigned: $isPassSigned")

    if (isDirectoryCreated && isPassSigned) {
      val passDataTuple = for {
        pkPassByteArray <- fileService.createPkPassZipForPass(path)
        qrCodeByteArray <- qrCodeService.createQRCode(s"${config.frontendServiceUrl}/get-pass-card?passId=$uuid&qr-code=true")
      } yield (pkPassByteArray, qrCodeByteArray)

      logger.info(s"[Creating Apple Pass] Zip and Qr Code Completed")
      fileService.deleteDirectory(path)
      passDataTuple.map(tuple => applePassRepository.insert(uuid, name, nino, tuple._1, tuple._2))
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
  val LABEL_NINO = "NATIONAL INSURANCE NUMBER"

  val KEY_NAME = "name"
  val LABEL_NAME = "NAME"

  val KEY_WARNING = "warning"
  val TEXT_WARNING = "This is not proof of your identity or your right to work in the UK."
}