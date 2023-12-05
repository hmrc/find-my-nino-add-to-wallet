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

import config.AppConfig
import models.GovUKPassDetails
import play.api.libs.json.Json
import play.api.{Configuration, Environment, Logging}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.GovUKPassService
import uk.gov.hmrc.auth.core.AuthConnector

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class GovUKPassController @Inject()(authConnector: AuthConnector,
                                    passService: GovUKPassService
                                   )(implicit config: Configuration,
                                     appConfig: AppConfig,
                                     env: Environment,
                                     cc: MessagesControllerComponents,
                                     ec: ExecutionContext) extends FMNBaseController(authConnector) with Logging {

  def createGovUKPass: Action[AnyContent] = Action.async { implicit request =>
    authorisedAsFMNUser { authContext => {
      if (appConfig.govukWalletEnabled) {

        //get the pass details from the request body
        val passDetails = request.body.asJson.get.as[GovUKPassDetails]

        Future(passService.createGovUKPass(passDetails.title, passDetails.givenName, passDetails.familyName, passDetails.nino) match {
          case Right(value) => Ok(Json.obj(
              "url" -> value._1,
              "bytes" -> value._2
            ))

          case Left(exp) => InternalServerError(Json.obj(
            "status" -> "500",
            "message" -> exp.getMessage
          ))
        })
      } else {
        Future.successful(Unauthorized(Json.obj(
          "status" -> "401",
          "message" -> "You cannot access this service with this account"
        )))
      }
    }}
  }

}
