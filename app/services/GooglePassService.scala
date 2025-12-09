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
import play.api.Logging
import repositories.GooglePassRepoTrait
import services.googlepass.GooglePassUtil

import java.io.ByteArrayInputStream
import java.util.{Base64, Collections, UUID}
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

trait GooglePassService {

  def getPassUrlByPassIdAndNINO(
    passId: String,
    nino: String
  )(implicit ec: ExecutionContext): Future[Option[String]]

  def getQrCodeByPassIdAndNINO(
    passId: String,
    nino: String
  )(implicit ec: ExecutionContext): Future[Option[Array[Byte]]]

  def createPass(
    name: String,
    nino: String,
    expirationDate: String
  )(implicit ec: ExecutionContext): Either[Exception, String]
}

@Singleton
class StubGooglePassService @Inject() (
  qrCodeService: QrCodeService
) extends GooglePassService
    with Logging {

  override def getPassUrlByPassIdAndNINO(
    passId: String,
    nino: String
  )(implicit ec: ExecutionContext): Future[Option[String]] = {
    logger.warn(s"[StubGooglePassService] getPassUrlByPassIdAndNINO stubbed for passId=$passId, nino=$nino")
    val googlePassUrl = "https://pay.google.com/gp/v/save/eyJhbGci6IkpXVCJ9"
    Future.successful(Some(googlePassUrl))
  }

  override def getQrCodeByPassIdAndNINO(
    passId: String,
    nino: String
  )(implicit ec: ExecutionContext): Future[Option[Array[Byte]]] = {
    logger.warn(s"[StubGooglePassService] getQrCodeByPassIdAndNINO stubbed for passId=$passId, nino=$nino")
    Future.successful(qrCodeService.createQRCode("/foo/bar"))
  }

  override def createPass(
    name: String,
    nino: String,
    expirationDate: String
  )(implicit ec: ExecutionContext): Either[Exception, String] = {
    val uuid = UUID.randomUUID().toString
    logger.warn(
      s"[StubGooglePassService] createPass stubbed for name=$name, nino=$nino, expirationDate=$expirationDate, uuid=$uuid"
    )
    Right(uuid)
  }
}

@Singleton
class RealGooglePassService @Inject() (
  val config: AppConfig,
  val googlePassUtil: GooglePassUtil,
  val googlePassRepository: GooglePassRepoTrait,
  val qrCodeService: QrCodeService
) extends GooglePassService
    with Logging {

  override def getPassUrlByPassIdAndNINO(
    passId: String,
    nino: String
  )(implicit ec: ExecutionContext): Future[Option[String]] =
    googlePassRepository.findByPassId(passId).map {
      case Some(googlePass) =>
        val session  = nino.replace(" ", "").take(8)
        val fromPass = googlePass.nino.replace(" ", "").take(8)

        if (session == fromPass)
          Some(googlePass.googlePassUrl)
        else {
          logger.warn("Pass NINO does not match session NINO")
          None
        }

      case None => None
    }

  override def getQrCodeByPassIdAndNINO(
    passId: String,
    nino: String
  )(implicit ec: ExecutionContext): Future[Option[Array[Byte]]] =
    googlePassRepository.findByPassId(passId).map {
      case Some(googlePass) =>
        val session  = nino.replace(" ", "").take(8)
        val fromPass = googlePass.nino.replace(" ", "").take(8)

        if (session == fromPass)
          Some(googlePass.qrCode)
        else {
          logger.warn("Pass NINO does not match session NINO")
          None
        }

      case None => None
    }

  override def createPass(
    name: String,
    nino: String,
    expirationDate: String
  )(implicit ec: ExecutionContext): Either[Exception, String] =
    try {
      val uuid  = UUID.randomUUID().toString
      val scope = "https://www.googleapis.com/auth/wallet_object.issuer"

      val keyAsStream = new ByteArrayInputStream(Base64.getDecoder.decode(config.googlePassKey.trim))

      val googleCreds =
        GoogleCredentials
          .fromStream(keyAsStream)
          .createScoped(Collections.singletonList(scope))

      val googlePassUrl =
        googlePassUtil.createGooglePassWithCredentials(name, nino, googleCreds)

      val qrCode =
        qrCodeService
          .createQRCode(s"${config.frontendServiceUrl}/get-google-pass?passId=$uuid&qr-code=true")
          .getOrElse(Array.emptyByteArray)

      googlePassRepository.insert(uuid, name, nino, expirationDate, googlePassUrl, qrCode)

      Right(uuid)
    } catch {
      case e: Exception =>
        logger.error("[RealGooglePassService] Error while creating Google Pass", e)
        Left(e)
    }
}
