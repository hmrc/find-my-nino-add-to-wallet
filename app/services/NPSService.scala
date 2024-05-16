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

import connectors.NPSConnector
import models.CorrelationId
import models.nps.CRNUpliftRequest
import play.api.Logging
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class NPSService @Inject()(connector: NPSConnector)(implicit val ec: ExecutionContext)
  extends Logging {

  def upliftCRN(identifier: String, crnUpliftRequest: CRNUpliftRequest
                )(implicit hc: HeaderCarrier, correlationId: CorrelationId, ec: ExecutionContext): Future[HttpResponse] = {
    connector.upliftCRN(identifier, crnUpliftRequest)
  }
}