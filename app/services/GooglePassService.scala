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

import com.google.auth.oauth2.GoogleCredentials
import config.AppConfig
import play.api.Logging
import repositories.GooglePassRepoTrait
import services.googlepass.GooglePassUtil

import java.util.UUID
import javax.inject.*
import scala.concurrent.{ExecutionContext, Future}

class GooglePassService @Inject() (
  val config: AppConfig,
  val googlePassUtil: GooglePassUtil,
  val googlePassRepository: GooglePassRepoTrait,
  val qrCodeService: QrCodeService
) extends Logging {

  private def ninoMatches(storedNino: String, sessionNino: String): Boolean =
    storedNino.replace(" ", "").take(8) == sessionNino.take(8)

  def getPassUrlByPassIdAndNINO(passId: String, nino: String)(implicit ec: ExecutionContext): Future[Option[String]] =
    googlePassRepository.findByPassId(passId).map {
      case Some(googlePass) if ninoMatches(googlePass.nino, nino) =>
        Some(googlePass.googlePassUrl)

      case Some(_) =>
        logger.warn("Pass NINO does not match session NINO")
        None

      case None =>
        None
    }

  def getQrCodeByPassIdAndNINO(passId: String, nino: String)(implicit
    ec: ExecutionContext
  ): Future[Option[Array[Byte]]] =
    googlePassRepository.findByPassId(passId).map {
      case Some(googlePass) if ninoMatches(googlePass.nino, nino) =>
        Some(googlePass.qrCode)

      case Some(_) =>
        logger.warn("Pass NINO does not match session NINO")
        None

      case None =>
        None
    }

  def createPassWithCredentials(
    name: String,
    nino: String,
    expirationDate: String,
    googleCredentials: GoogleCredentials
  )(implicit ec: ExecutionContext): Future[Either[Exception, String]] = {

    val uuid                  = UUID.randomUUID().toString
    val googlePassUrl: String = googlePassUtil.createGooglePassWithCredentials(name, nino, googleCredentials)

    val qrCode: Array[Byte] = qrCodeService
      .createQRCode(s"${config.frontendServiceUrl}/get-google-pass?passId=$uuid&qr-code=true")
      .getOrElse(Array.emptyByteArray)

    googlePassRepository
      .insert(uuid, name, nino, expirationDate, googlePassUrl, qrCode)
      .map(_ => Right(uuid))
      .recover { case e =>
        Left(new Exception("Problem occurred while storing Google Pass.", e))
      }
  }
}
