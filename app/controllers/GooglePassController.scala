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

package controllers

import connectors.FandFConnector
import models.google.GooglePassDetails
import play.api.libs.json.{Json, OFormat, Writes}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Environment, Logging}
import services.GooglePassService
import uk.gov.hmrc.auth.core.AuthConnector

import java.time.{ZoneId, ZonedDateTime}
import java.util.Base64
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton()
class GooglePassController @Inject() (
  authConnector: AuthConnector,
  fandfConnector: FandFConnector,
  passService: GooglePassService
)(implicit
  config: Configuration,
  env: Environment,
  cc: MessagesControllerComponents,
  ec: ExecutionContext
) extends FMNBaseController(authConnector, fandfConnector)
    with Logging {

  implicit val passRequestFormatter: OFormat[GooglePassDetails] = Json.format[GooglePassDetails]
  implicit val writes: Writes[GooglePassDetails]                = Json.writes[GooglePassDetails]

  // shall we configure it in application.conf file
  private val DEFAULT_EXPIRATION_YEARS = 100

  // $COVERAGE-OFF$
  def createPassWithCredentials: Action[AnyContent] = Action.async { implicit request =>
    authorisedAsFMNUser { _ =>
      request.body.asJson match {
        case Some(json) =>
          val passRequest    = json.as[GooglePassDetails]
          val expirationDate = ZonedDateTime.now(ZoneId.of("UTC")).plusYears(DEFAULT_EXPIRATION_YEARS).toString

          logger.info(
            s"[GooglePassController] Creating Google Pass for nino=${passRequest.nino}, expiry=$expirationDate"
          )

          passService.createPass(passRequest.fullName, passRequest.nino, expirationDate) match {
            case Right(value) => Future.successful(Ok(value))
            case Left(exp)    =>
              logger.error("[GooglePassController] Error creating Google Pass", exp)
              Future.successful(
                InternalServerError(
                  Json.obj(
                    "status"  -> "500",
                    "message" -> exp.getMessage
                  )
                )
              )
          }

        case None =>
          Future.successful(BadRequest("Expected JSON body"))
      }
    }
  }
  // $COVERAGE-ON$

  def getPassUrlByPassId(passId: String): Action[AnyContent] = Action.async { implicit request =>
    authorisedAsFMNUser { authContext =>
      passService.getPassUrlByPassIdAndNINO(passId, authContext.nino).map {
        case Some(data) => Ok(data)
        case _          => NotFound
      }
    }
  }

  def getQrCodeByPassId(passId: String): Action[AnyContent] = Action.async { implicit request =>
    authorisedAsFMNUser { authContext =>
      passService.getQrCodeByPassIdAndNINO(passId, authContext.nino).map {
        case Some(data) => Ok(Base64.getEncoder.encodeToString(data))
        case _          => NotFound
      }
    }
  }
}
