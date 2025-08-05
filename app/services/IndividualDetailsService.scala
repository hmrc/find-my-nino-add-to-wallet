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

package services
import cats.data.EitherT
import connectors.IndividualDetailsConnector
import play.api.libs.json.JsValue
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, UpstreamErrorResponse}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class IndividualDetailsService @Inject() (individualDetailsConnector: IndividualDetailsConnector)(implicit
  val ec: ExecutionContext
) {

  def getIndividualDetails(nino: String, resolveMerge: String)(implicit
    hc: HeaderCarrier
  ): EitherT[Future, UpstreamErrorResponse, JsValue] =
    individualDetailsConnector.getIndividualDetails(nino, resolveMerge)

}
