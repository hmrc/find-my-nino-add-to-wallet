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

import config.AppConfig
import models.CorrelationId
import models.nps.ChildReferenceNumberUpliftRequest
import play.api.Logging
import play.api.http.MimeTypes
import play.api.libs.ws.WSBodyWritables.writeableOf_JsValue
import services.AuditService
import uk.gov.hmrc.http.client.HttpClientV2

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import java.net.URL
import uk.gov.hmrc.http.HttpReads.Implicits._
import util.AuditUtils

class NPSConnector @Inject() (httpClientV2: HttpClientV2, appConfig: AppConfig, auditService: AuditService)
    extends Logging {

  def upliftCRN(identifier: String, request: ChildReferenceNumberUpliftRequest)(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[HttpResponse] = {

    val len: Int = 8

    val childReferenceNumber: String = identifier.take(len)
    val auditType: String            = "ChildReferenceNumberUplift"
    val appName: String              = appConfig.appName
    val correlationId: String        = CorrelationId.random.value.toString

    val url =
      s"${appConfig.npsCrnUrl}/nps/nps-json-service/nps/v1/api/individual/$childReferenceNumber/adult-registration"

    val headers = Seq(
      (play.api.http.HeaderNames.CONTENT_TYPE, MimeTypes.JSON),
      (play.api.http.HeaderNames.AUTHORIZATION, s"Basic ${appConfig.npsCrnToken}"),
      (appConfig.npsCrnCorrelationIdKey, correlationId),
      (appConfig.npsCrnOriginatorIdKey, appConfig.npsCrnOriginatorIdValue)
    )

    val httpResponse = httpClientV2
      .put(new URL(url))
      .withBody(request)
      .setHeader(headers: _*)
      .execute[HttpResponse]
      .flatMap { response =>
        auditService.audit(
          AuditUtils.childReferenceNumberUplift(url, request, response, auditType, appName, correlationId)
        )
        Future.successful(response)
      }

    httpResponse
  }
}
