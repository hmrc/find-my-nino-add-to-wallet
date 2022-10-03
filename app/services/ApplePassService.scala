/*
 * Copyright 2022 HM Revenue & Customs
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

import java.nio.file.Paths
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

  def createPass(name: String, nino: String)(implicit ec: ExecutionContext): Either[Exception, String] = {
    val uuid = UUID.randomUUID().toString
    val passDirectory = config.passPath
    val path = Paths.get(s"$passDirectory/$uuid.pass")

    val pass = ApplePassCard(name, nino, uuid)
    val isDirectoryCreated = fileService.createDirectoryForPass(path, pass)
    val isPassSigned = signatureService.createSignatureForPass(path, config.privateCertificate, config.privateCertificatePassword, config.appleWWDRCA)

    if (isDirectoryCreated && isPassSigned) {
      val passDataTuple = for {
        pkPassByteArray <- fileService.createPkPassZipForPass(path)
        qrCodeByteArray <- qrCodeService.createQRCode(s"${config.serviceUrl}/get-pass-details?passId=$uuid")
      } yield (pkPassByteArray, qrCodeByteArray)

      fileService.deleteDirectory(path)
      passDataTuple.map(tuple => applePassRepository.insert(uuid, name, nino, tuple._1, tuple._2))
      Right(uuid)
    } else {
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
  val TEXT_WARNING = "This is not proof of identity"
}