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

import com.google.auth.oauth2.GoogleCredentials
import config.AppConfig
import models.GooglePassDetails
import play.api.Logging
import repositories.GooglePassRepository
import googlepass.GooglePassUtil

import java.util.UUID
import javax.inject._
import scala.concurrent.{ExecutionContext, Future}

class GooglePassService @Inject()(val config: AppConfig,
                                  val googlePassUtil: GooglePassUtil,
                                  val googlePassRepository: GooglePassRepository,
                                  val qrCodeService: QrCodeService) extends Logging {



  def getPassUrlByPassIdAndNINO(passId: String, nino: String)(implicit ec: ExecutionContext): Future[Option[String]] = {
    for {
      gp <- googlePassRepository.findByPassId(passId)
    } yield {
      gp match {
        case Some(googlePass) => {
          if (googlePass.nino.replace(" ", "").equals(nino)) {
            Some(googlePass.googlePassUrl)
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
      gqrCode <- googlePassRepository.findByPassId(passId)
    } yield {
      gqrCode match {
        case Some(googlePass) => {
          if (googlePass.nino.replace(" ","").equals(nino)) {
            Some(googlePass.qrCode)
          } else {
            logger.warn("Pass NINO does not match session NINO")
            None
          }
        }
        case _ => None
      }
    }
  }

  def getPassDetails(passId: String, nino: String)(implicit ec: ExecutionContext): Future[Option[GooglePassDetails]] = {
    for {
      gpDetails <- googlePassRepository.findByPassId(passId)
    } yield {
      gpDetails match {
        case Some(googlePass) => {
          if (googlePass.nino.replace(" ","").equals(nino)) {
            Some(GooglePassDetails(googlePass.fullName, googlePass.nino))
          } else {
            logger.warn("Pass NINO does not match session NINO")
            None
          }
        }
        case _ => None
      }
    }
  }

  def getPassDetailsWithNameAndNino(fullName: String, nino: String)(implicit ec: ExecutionContext): Future[Option[GooglePassDetails]] = {
    googlePassRepository.findByNameAndNino(fullName, nino).map(_.map(r => GooglePassDetails(r.fullName, r.nino)))
  }

  def createPassWithCredentials(name: String,
                                nino: String,
                                expirationDate: String,
                                googleCredentials: GoogleCredentials)(implicit ec: ExecutionContext): Either[Exception, String] = {
    val uuid = UUID.randomUUID().toString
    val googlePassUrl: String = googlePassUtil.createGooglePassWithCredentials(name, nino, googleCredentials)
    val qrCode = qrCodeService.createQRCode(s"${config.frontendServiceUrl}/get-google-pass?passId=$uuid&qr-code=true").getOrElse(Array.emptyByteArray)
    googlePassRepository.insert(uuid, name, nino, expirationDate, googlePassUrl, qrCode)
    Right(uuid)
  }

}