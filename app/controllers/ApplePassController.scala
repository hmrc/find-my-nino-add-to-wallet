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

import models.ApplePassDetails
import play.api.libs.json.{Json, OFormat, Writes}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Environment, Logging}
import services.ApplePassService
import uk.gov.hmrc.auth.core.AuthConnector

import java.util.Base64
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton()
class ApplePassController @Inject()(
                                     authConnector: AuthConnector,
                                     passService: ApplePassService)(implicit
                                                                    config: Configuration,
                                                                    env: Environment,
                                                                    cc: MessagesControllerComponents,
                                                                    ec: ExecutionContext) extends FMNBaseController(authConnector) with Logging {

  implicit val passRequestFormatter: OFormat[ApplePassDetails] = Json.format[ApplePassDetails]
  implicit val writes: Writes[ApplePassDetails] = Json.writes[ApplePassDetails]
  private val DEFAULT_EXPIRATION_YEARS = 100

  def createPass: Action[AnyContent] = Action.async { implicit request =>
    authorisedAsFMNUser { authContext => {
      val passRequest = request.body.asJson.get.as[ApplePassDetails]

      logger.debug(message = s"[Create Pass Event]$passRequest")
      Future(passService.createPass(passRequest.fullName, passRequest.nino) match {
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
      logger.debug(message = s"[Get Pass Details] $passId")
      passService.getPassDetails(passId).map {
        case Some(data) => Ok(Json.toJson(data))
        case _ => NotFound
      }
    }
    }
  }

  def getPassDetailsWithNameAndNino(fullName: String, nino: String): Action[AnyContent] = Action.async { implicit request =>
    authorisedAsFMNUser { authContext => {
      logger.debug(message = s"[Get Pass Details] fullName=$fullName, nino=$nino")
      passService.getPassDetailsWithNameAndNino(fullName, nino).map {
        case Some(data) => Ok(Json.toJson(data))
        case _ => NotFound
      }
    }
    }
  }

  def getPassCardByPassId(passId: String): Action[AnyContent] = Action.async { implicit request =>
    authorisedAsFMNUser { authContext => {
      logger.debug(message = s"[Get Pass Card] $passId")
      passService.getPassCardByPassId(passId).map {
        case Some(data) => Ok(Base64.getEncoder.encodeToString(data))
        case _ => NotFound
      }
    }
    }
  }

  def getQrCodeByPassId(passId: String): Action[AnyContent] = Action.async { implicit request =>
    authorisedAsFMNUser { authContext => {
      logger.debug(message = s"[Get QR Code] $passId")
      passService.getQrCodeByPassId(passId).map {
        case Some(data) => Ok(Base64.getEncoder.encodeToString(data))
        case _ => NotFound
      }
    }
    }
  }
}
