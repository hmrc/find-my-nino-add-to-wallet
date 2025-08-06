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

import cats.data.EitherT
import connectors.FandFConnector
import play.api.http.Status.{NOT_FOUND, UNAUTHORIZED}
import play.api.libs.json.{JsError, JsResultException, JsSuccess, JsValue}
import play.api.mvc.*
import play.api.mvc.Results.{InternalServerError, NotFound, Unauthorized}
import play.api.{Configuration, Environment}
import services.IndividualDetailsService
import transformations.IndividualDetails
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton()
class IndividualsDetailsController @Inject() (
  authConnector: AuthConnector,
  fandFConnector: FandFConnector,
  individualDetailsService: IndividualDetailsService
)(implicit config: Configuration, env: Environment, cc: MessagesControllerComponents, ec: ExecutionContext)
    extends FMNBaseController(authConnector, fandFConnector) {

  def getIndividualDetails(nino: String, resolveMerge: String): Action[AnyContent] = Action.async { implicit request =>
    authorisedAsFMNUser { authContext =>
      implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
      val ninoLengthWithoutSuffix    = 8
      if (authContext.nino.take(ninoLengthWithoutSuffix) != nino.take(ninoLengthWithoutSuffix)) {
        logger.warn(s"User with NINO ${authContext.nino} is trying to access NINO $nino")
        Future(Results.Unauthorized("You are not authorised to access this resource"))
      } else {
        resultFromStatus(individualDetailsService.getIndividualDetails(nino, resolveMerge))
      }
    }
  }

  def deleteCachedIndividualDetails(nino: String): Action[AnyContent] = Action.async { implicit request =>
    authorisedAsFMNUser { authContext =>
      implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
      val ninoLengthWithoutSuffix    = 8
      if (authContext.nino.take(ninoLengthWithoutSuffix) != nino.take(ninoLengthWithoutSuffix)) {
        logger.warn(s"User with NINO ${authContext.nino} is trying to access NINO $nino")
        Future(Results.Unauthorized("You are not authorised to access this resource"))
      } else {

        individualDetailsService
          .deleteIndividualDetails(nino)
          .bimap(errorToResponse, _ => Ok)
          .merge

      }
    }
  }

  private def resultFromStatus(response: EitherT[Future, UpstreamErrorResponse, JsValue]): Future[Result] =
    response
      .bimap(
        errorToResponse,
        jsValue =>
          jsValue.transform(IndividualDetails.reads) match {
            case JsSuccess(jsObject, _) => Ok(jsObject)
            case JsError(errors)        =>
              val ex = JsResultException(errors)
              logger.error("Json transformation failure", ex)
              Results.InternalServerError(ex.getMessage)
          }
      )
      .merge

  private def errorToResponse(error: UpstreamErrorResponse): Result =
    error match {
      case UpstreamErrorResponse(erMesssage, BAD_REQUEST, _, _)           => BadRequest(erMesssage)
      case UpstreamErrorResponse(erMesssage, UNAUTHORIZED, _, _)          => Unauthorized(erMesssage)
      case UpstreamErrorResponse(erMesssage, NOT_FOUND, _, _)             => NotFound(erMesssage)
      case UpstreamErrorResponse(erMesssage, INTERNAL_SERVER_ERROR, _, _) => InternalServerError(erMesssage)
      case UpstreamErrorResponse(erMesssage, NOT_IMPLEMENTED, _, _)       => NotImplemented(erMesssage)
      case UpstreamErrorResponse(erMesssage, status, _, _)                => Results.Status(status)
    }
}
