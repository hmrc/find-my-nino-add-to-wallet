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

import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result, Results}
import play.api.{Configuration, Environment}
import services.IndividualDetailsService
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton()
class IndividualsDetailsController  @Inject()(authConnector: AuthConnector,
                                              individualDetailsService: IndividualDetailsService
                                             )(implicit
                                               config: Configuration,
                                               env: Environment,
                                               cc: MessagesControllerComponents,
                                               ec: ExecutionContext) extends FMNBaseController(authConnector) {

  def getIndividualDetails(nino: String, resolveMerge: String): Action[AnyContent] = Action.async {
    implicit request =>
      authorisedAsFMNUser {
        authContext => {
          implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
          val ninoLengthWithoutSuffix = 8
          if(authContext.nino.value.take(ninoLengthWithoutSuffix) != nino.take(ninoLengthWithoutSuffix)) {
            logger.warn(s"User with NINO ${authContext.nino.value} is trying to access NINO $nino")
            Future(Results.Unauthorized("You are not authorised to access this resource"))
          }else {
            individualDetailsService.getIndividualDetails(nino, resolveMerge).map(resultFromStatus)
          }
        }
      }
  }

  private def resultFromStatus(response: HttpResponse): Result = {
    response.status match {
      case OK => Results.Ok(response.body)
      case BAD_REQUEST => Results.BadRequest(response.body)
      case UNAUTHORIZED => Results.Unauthorized(response.body)
      case NOT_FOUND => Results.NotFound(response.body)
      case INTERNAL_SERVER_ERROR => Results.InternalServerError(response.body)
      case NOT_IMPLEMENTED => Results.NotImplemented(response.body)
      case status => Results.Status(status)(response.body)
    }
  }
}

