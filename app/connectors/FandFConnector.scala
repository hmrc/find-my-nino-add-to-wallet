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

package connectors

import com.google.inject.Inject
import config.AppConfig
import play.api.Logging
import play.api.http.Status.{NOT_FOUND, OK}
import uk.gov.hmrc.auth.core.retrieve.v2.TrustedHelper
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps, UpstreamErrorResponse}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal
import scala.util.{Failure, Success, Try}

class FandFConnector @Inject()(
  val httpClient: HttpClientV2,
  appConfig: AppConfig
)(implicit val ec: ExecutionContext)
    extends Logging {

  def getTrustedHelper()(implicit hc: HeaderCarrier): Future[Option[TrustedHelper]] =
    httpClient
      .get(url"${appConfig.fandfServiceUrl}/delegation/get")
      .execute[HttpResponse]
      .map { httpResponse =>
        httpResponse.status match {
          case NOT_FOUND => None
          case OK        =>
            Try(httpResponse.json.as[TrustedHelper](uk.gov.hmrc.auth.core.retrieve.v2.TrustedHelper.reads)) match {
              case Success(trustedHelper) => Some(trustedHelper)
              case Failure(ex)            =>
                logger.error(s"Failed to parse TrustedHelper", ex)
                None
            }
          case status    =>
            val ex = UpstreamErrorResponse("Invalid response status", status)
            logger.error(ex.message, ex)
            None
        }
      }
      .recover { case NonFatal(ex) =>
        logger.error(s"Exception: ${ex.getMessage}", ex)
        None
      }
}
