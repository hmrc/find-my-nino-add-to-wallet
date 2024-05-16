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

import auth.FMNAuth
import models.CorrelationId
import models.nps.CRNUpliftRequest
import play.api.{Configuration, Environment, Logging}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.{JsValue, Json, OFormat}
import play.api.mvc.{Action, MessagesControllerComponents, Results}
import services.NPSService
import uk.gov.hmrc.auth.core.{AuthConnector, AuthorisedFunctions}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendBaseController

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class NPSController @Inject()(
                                  override val messagesApi: MessagesApi,
                                  val controllerComponents: MessagesControllerComponents,
                                  val authConnector: AuthConnector,
                                  npsService: NPSService
                                )(implicit val config: Configuration,
                                  executionContext: ExecutionContext,
                                  val env: Environment
                                ) extends BackendBaseController with FMNAuth with AuthorisedFunctions with I18nSupport with Logging {

  implicit val format: OFormat[CRNUpliftRequest] = Json.format[CRNUpliftRequest]

  def upliftCrn(identifier: String): Action[JsValue] = Action.async(parse.json) { implicit request =>
    authorisedAsFMNUser { _ => {

      val passRequest = request.body.as[CRNUpliftRequest]
      implicit val correlationId: CorrelationId = CorrelationId.random
      for {
        httpResponse <- npsService.upliftCRN(identifier, passRequest)
      } yield httpResponse.status match {
        case 204 => Results.NoContent
        case 400 => Results.BadRequest(httpResponse.body)
        case 403 => Results.Forbidden(httpResponse.body)
        case 422 => Results.UnprocessableEntity(httpResponse.body)
        case 404 => Results.NotFound
        case 500 => Results.InternalServerError
        case _ => Results.Status(httpResponse.status)(httpResponse.body)
      }
    }
    }
  }
}