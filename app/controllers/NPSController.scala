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

import models.nps.ChildReferenceNumberUpliftRequest
import play.api.mvc._
import play.api.{Configuration, Environment, Logging}
import services.NPSService
import uk.gov.hmrc.auth.core.AuthConnector

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton()
class NPSController @Inject() (authConnector: AuthConnector, npsService: NPSService)(implicit
  config: Configuration,
  env: Environment,
  cc: MessagesControllerComponents,
  ec: ExecutionContext
) extends FMNBaseController(authConnector)
    with Logging {

  def upliftCRN(identifier: String): Action[AnyContent] = Action.async { implicit request =>
    authorisedAsFMNUser { _ =>
      val upliftRequest: ChildReferenceNumberUpliftRequest =
        request.body.asJson.get.as[ChildReferenceNumberUpliftRequest]
      for {
        httpResponse <- npsService.upliftCRN(identifier, upliftRequest)
      } yield httpResponse.status match {
        case NO_CONTENT            => Results.NoContent
        case BAD_REQUEST           => Results.BadRequest(httpResponse.body)
        case FORBIDDEN             => Results.Forbidden(httpResponse.body)
        case UNPROCESSABLE_ENTITY  => Results.UnprocessableEntity(httpResponse.body)
        case NOT_FOUND             => Results.NotFound
        case INTERNAL_SERVER_ERROR => Results.InternalServerError
        case _                     => Results.Status(httpResponse.status)(httpResponse.body)
      }
    }
  }
}
