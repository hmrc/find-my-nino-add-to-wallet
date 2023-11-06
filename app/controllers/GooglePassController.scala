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

package controllers

import com.google.auth.oauth2.GoogleCredentials
import models.{GooglePassDetails, GooglePassDetailsWithCredentials}
import org.joda.time.{DateTime, DateTimeZone}
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.{Json, OFormat, Writes}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Environment, Logging}
import services.{DeSerializer, GooglePassService}
import uk.gov.hmrc.auth.core.AuthConnector

import java.util.Base64
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton()
class GooglePassController @Inject()(
                                      authConnector: AuthConnector,
                                      passService: GooglePassService)(implicit
                                                                      config: Configuration,
                                                                      env: Environment,
                                                                      cc: MessagesControllerComponents,
                                                                      ec: ExecutionContext) extends FMNBaseController(authConnector) with Logging {

  implicit val passRequestFormatter: OFormat[GooglePassDetails] = Json.format[GooglePassDetails]
  implicit val passRequestFormatterWithCredentials: OFormat[GooglePassDetailsWithCredentials] = Json.format[GooglePassDetailsWithCredentials]

  implicit val writes: Writes[GooglePassDetails] = Json.writes[GooglePassDetails]
  implicit val writesWithCredentials: Writes[GooglePassDetailsWithCredentials] = Json.writes[GooglePassDetailsWithCredentials]

  // shall we configure it in application.conf file
  private val DEFAULT_EXPIRATION_YEARS = 100

  def createPassWithCredentials: Action[AnyContent] = Action.async { implicit request =>
    authorisedAsFMNUser { authContext => {
      val passRequest = request.body.asJson.get.as[GooglePassDetailsWithCredentials]
      val expirationDate = DateTime.now(DateTimeZone.UTC).plusYears(DEFAULT_EXPIRATION_YEARS)
      val googleCredentials = DeSerializer.deserializeObjectFromBase64[GoogleCredentials](passRequest.credentials)

      Future(passService.createPassWithCredentials(passRequest.fullName, passRequest.nino, expirationDate.toString(), googleCredentials) match {
        case Right(value) => Ok(value)
        case Left(exp) => InternalServerError(Json.obj(
          "status" -> "500",
          "message" -> exp.getMessage
        ))
      })
    }
    }
  }

  def getPassDetails(passId: String): Action[AnyContent] = Action.async { implicit request =>
    authorisedAsFMNUser { authContext => {
      passService.getPassDetails(passId, authContext.nino.value).map {
        case Some(data) => Ok(Json.toJson(data))
        case _ => NotFound
      }
    }
    }
  }

  def getPassDetailsWithNameAndNino(fullName: String, nino: String): Action[AnyContent] = Action.async { implicit request =>
    authorisedAsFMNUser { authContext => {
      passService.getPassDetailsWithNameAndNino(fullName, nino).map {
        case Some(data) => Ok(Json.toJson(data))
        case _ => NotFound
      }
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
