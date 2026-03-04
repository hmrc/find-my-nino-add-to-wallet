/*
 * Copyright 2026 HM Revenue & Customs
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

import java.util.UUID
import javax.inject.*
import scala.concurrent.{ExecutionContext, Future}

class ApplePassService @Inject() (
  val config: AppConfig,
  val applePassRepository: ApplePassRepoTrait,
  val fileService: FileService,
  val signatureService: SignatureService,
  val qrCodeService: QrCodeService
) extends Logging {

  private val signingEnabled: Boolean = config.applePassSigningEnabled

  private def ninoMatches(storedNino: String, sessionNino: String): Boolean =
    storedNino.replace(" ", "").take(8) == sessionNino.take(8)

  def getPassCardByPassIdAndNINO(passId: String, nino: String)(implicit
    ec: ExecutionContext
  ): Future[Option[Array[Byte]]] =
    applePassRepository.findByPassId(passId).map {
      case Some(applePass) if ninoMatches(applePass.nino, nino) =>
        Some(applePass.applePassCard)

      case Some(_) =>
        logger.warn("Pass NINO does not match session NINO")
        None

      case None =>
        None
    }

  def getQrCodeByPassIdAndNINO(passId: String, nino: String)(implicit
    ec: ExecutionContext
  ): Future[Option[Array[Byte]]] =
    applePassRepository.findByPassId(passId).map {
      case Some(applePass) if ninoMatches(applePass.nino, nino) =>
        Some(applePass.qrCode)

      case Some(_) =>
        logger.warn("Pass NINO does not match session NINO")
        None

      case None =>
        None
    }

  def createPass(name: String, nino: String)(implicit ec: ExecutionContext): EitherT[Future, Exception, String] =
    EitherT {
      val uuid = UUID.randomUUID().toString
      val pass = ApplePassCard(name, nino, uuid)

      val passFilesInBytes = fileService.createFileBytesForPass(pass)

      if (passFilesInBytes.isEmpty) {
        Future.successful(
          Left(new Exception("Problem occurred while creating Apple Pass. Pass files generated: false"))
        )
      } else {

        val signatureF: Future[FileAsBytes] =
          if (!signingEnabled) {
            Future.successful(FileAsBytes(SignatureService.SIGNATURE_FILE_NAME, Array.emptyByteArray))
          } else {
            config.appleCerts.map { certs =>
              signatureService.createSignatureForPass(
                passFilesInBytes,
                certs.privateCert,
                certs.privateCertPassword,
                certs.wwdrca
              )
            }
          }

        signatureF.flatMap { signature =>
          val signatureOk = !signingEnabled || signature.content.nonEmpty

          if (!signatureOk) {
            logger.error(
              s"[Creating Apple Pass] Signature failed. isPassFilesGenerated: ${passFilesInBytes.nonEmpty} || isPassSigned: false"
            )
            Future.successful(
              Left(
                new Exception(
                  s"Problem occurred while creating Apple Pass. Pass files generated: true, Pass files signed: false"
                )
              )
            )
          } else {

            val passDataOpt =
              for {
                pkPassByteArray <- fileService.createPkPassZipForPass(passFilesInBytes, signature)
                qrCodeByteArray <-
                  qrCodeService.createQRCode(s"${config.frontendServiceUrl}/get-pass-card?passId=$uuid&qr-code=true")
              } yield (pkPassByteArray, qrCodeByteArray)

            passDataOpt match {
              case Some((pkPass, qrCode)) =>
                applePassRepository
                  .insert(uuid, name, nino, pkPass, qrCode)
                  .map(_ => Right(uuid))
                  .recover { case e =>
                    Left(new Exception("Problem occurred while storing Apple Pass.", e))
                  }

              case None =>
                logger.error(
                  s"[Creating Apple Pass] Zip/QRCode generation failed. " +
                    s"isPassFilesGenerated: ${passFilesInBytes.nonEmpty} || isPassSigned: ${signature.content.nonEmpty}"
                )
                Future.successful(
                  Left(
                    new Exception(
                      s"Problem occurred while creating Apple Pass. " +
                        s"Pass files generated: ${passFilesInBytes.nonEmpty}, Pass files signed: ${signature.content.nonEmpty}"
                    )
                  )
                )
            }
          }
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
