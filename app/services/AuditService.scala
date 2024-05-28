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

import play.api.Logging
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.{AuditConnector, AuditResult}
import uk.gov.hmrc.play.audit.model.ExtendedDataEvent

import javax.inject.Inject
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

class AuditService @Inject()(auditConnector: AuditConnector, implicit val ec: ExecutionContext) extends Logging {

  def audit(evt: ExtendedDataEvent)(implicit
                                    hc: HeaderCarrier
  ): Unit =
    auditConnector.sendExtendedEvent(evt).onComplete {
      case Success(AuditResult.Success)         => logger.debug(s"Sent audit event: ${evt.toString}")
      case Failure(AuditResult.Failure(msg, _)) => logger.warn(s"Could not audit ${evt.auditType}: $msg")
      case Failure(ex)                          => logger.warn(s"Could not audit ${evt.auditType}: ${ex.getMessage}")
      case _                                    => ()
    }
}