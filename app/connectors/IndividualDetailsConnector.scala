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

import com.google.inject.{Inject, Singleton}
import config.AppConfig
import models.CorrelationId
import play.api.Logging
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class IndividualDetailsConnector @Inject() (val httpClientV2: HttpClientV2, appConfig: AppConfig) extends Logging {

  private val extraDesHeaders: Seq[(String, String)] = Seq(
    "Authorization" -> s"Bearer ${appConfig.individualDetailsToken}",
    "CorrelationId" -> CorrelationId.random.value.toString,
    "Content-Type"  -> "application/json",
    "Environment"   -> appConfig.individualDetailsEnvironment,
    "OriginatorId"  -> appConfig.individualDetailsOriginatorId
  )

  def getIndividualDetails(nino: String, resolveMerge: String)(implicit
    ec: ExecutionContext,
    headerCarrier: HeaderCarrier
  ): Future[HttpResponse] = {

    implicit val hc: HeaderCarrier = headerCarrier.withExtraHeaders(extraDesHeaders: _*)
    val url                        =
      s"${appConfig.individualDetailsServiceUrl}/individuals/details/NINO/${nino.take(8)}?resolveMerge=$resolveMerge"

    httpClientV2
      .get(url"$url")
      .execute[HttpResponse]
  }
}
