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

package controllers

import com.google.auth.oauth2.GoogleCredentials
import config.AppConfig
import models.google.GooglePassDetails
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.{Json, OFormat, Writes}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Environment, Logging}
import services.GooglePassService
import uk.gov.hmrc.auth.core.AuthConnector

import java.io.ByteArrayInputStream
import java.time.{ZoneId, ZonedDateTime}
import java.util.{Base64, Collections}
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton()
class GooglePassController @Inject()(
                                      authConnector: AuthConnector,
                                      passService: GooglePassService)(implicit
                                                                      config: Configuration,
                                                                      env: Environment,
                                                                      cc: MessagesControllerComponents,
                                                                      appConfig: AppConfig,
                                                                      ec: ExecutionContext) extends FMNBaseController(authConnector) with Logging {

  implicit val passRequestFormatter: OFormat[GooglePassDetails] = Json.format[GooglePassDetails]

  implicit val writes: Writes[GooglePassDetails] = Json.writes[GooglePassDetails]

  // shall we configure it in application.conf file
  private val DEFAULT_EXPIRATION_YEARS = 100

  def createPassWithCredentials: Action[AnyContent] = Action.async { implicit request =>
    authorisedAsFMNUser { authContext => {
      val passRequest = request.body.asJson.get.as[GooglePassDetails]
      val expirationDate = ZonedDateTime.now(ZoneId.of("UTC")).plusYears(DEFAULT_EXPIRATION_YEARS)

      val scope = "https://www.googleapis.com/auth/wallet_object.issuer"
      val keyAsStream = new ByteArrayInputStream(Base64.getDecoder.decode(appConfig.googleKey))
      val googleCredentials: GoogleCredentials = GoogleCredentials.fromStream(keyAsStream).createScoped(Collections.singletonList(scope))

      Future(passService.createPassWithCredentials(passRequest.fullName, passRequest.nino, expirationDate.toString, googleCredentials) match {
        case Right(value) => Ok(value)
        case Left(exp) => InternalServerError(Json.obj(
          "status" -> "500",
          "message" -> exp.getMessage
        ))
      })
    }
    }
  }

  def getPassUrlByPassId(passId: String): Action[AnyContent] = Action.async { implicit request =>
    authorisedAsFMNUser { authContext => {
      passService.getPassUrlByPassIdAndNINO(passId,authContext.nino.value).map {
        case Some(data) => Ok(data)
        case _ => NotFound
      }
    }
    }
  }

  def getQrCodeByPassId(passId: String): Action[AnyContent] = Action.async { implicit request =>
    authorisedAsFMNUser { authContext => {
      passService.getQrCodeByPassIdAndNINO(passId,authContext.nino.value).map {
        case Some(data) => Ok(Base64.getEncoder.encodeToString(data))
        case _ => NotFound
      }
    }
    }
  }
}
