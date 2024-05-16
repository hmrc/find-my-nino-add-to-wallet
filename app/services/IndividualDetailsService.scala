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

package services
import config.AppConfig
import connectors.IndividualDetailsConnector
import models.CorrelationId
import play.api.http.Status._
import play.api.mvc.{Result, Results}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class IndividualDetailsService @Inject()(appConfig: AppConfig,
                                         individualDetailsConnector: IndividualDetailsConnector
                                        )(
  implicit val ec:ExecutionContext) {

  private val extraDesHeaders = Seq(
    "Authorization" -> s"Bearer ${appConfig.individualDetailsToken}",
    "CorrelationId" -> CorrelationId.random.value.toString,
    "Content-Type" -> "application/json",
    "Environment" -> appConfig.individualDetailsEnvironment,
    "OriginatorId" -> appConfig.individualDetailsOriginatorId
  )
  //val desHeaders:HeaderCarrier = hc.withExtraHeaders(extraDesHeaders: _*)


  def getIndividualDetails(nino:String, resolveMerge:String)(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    val desHeaders:HeaderCarrier = hc.withExtraHeaders(extraDesHeaders: _*)
    individualDetailsConnector.getIndividualDetails(nino, resolveMerge, desHeaders)
  }

}
