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

  def getPassUrlByPassId(passId: String)(implicit ec: ExecutionContext): Future[Option[String]] = {
    googlePassRepository.findByPassId(passId).map(_.map(_.googlePassUrl))
  }

  def getQrCodeByPassId(passId: String)(implicit ec: ExecutionContext): Future[Option[Array[Byte]]] = {
    googlePassRepository.findByPassId(passId).map(_.map(_.qrCode))
  }

  def getPassDetails(passId: String)(implicit ec: ExecutionContext): Future[Option[GooglePassDetails]] = {
    googlePassRepository.findByPassId(passId).map(_.map(r => GooglePassDetails(r.fullName, r.nino)))
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