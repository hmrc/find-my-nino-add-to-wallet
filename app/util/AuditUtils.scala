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

package util

import models.nps.ChildRecordNumberUpliftRequest
import play.api.libs.json.{JsValue, Json, OFormat}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, NotFoundException}
import uk.gov.hmrc.play.audit.model.ExtendedDataEvent

import java.time.format.DateTimeFormatter
import java.time.{LocalDate, ZoneOffset}

object AuditUtils {

  val auditSource = "find-my-nino-add-to-wallet"

  case class ChildRecordNumberUpliftAuditEvent(
                                                journeyId: String,
                                                formCreationTimestamp: String,
                                                url: String,
                                                childRecordNumberUpliftRequest: ChildRecordNumberUpliftRequest,
                                                childRecordNumberUpliftResponseStatus: String,
                                                childRecordNumberUpliftResponseBody: String
                                )

  object ChildRecordNumberUpliftAuditEvent {
    implicit val format: OFormat[ChildRecordNumberUpliftAuditEvent] = Json.format[ChildRecordNumberUpliftAuditEvent]
  }

  private def getReferer(hc: HeaderCarrier): String = hc.otherHeaders.toMap.getOrElse("Referer", "")

  private def buildDataEvent(auditType: String, transactionName: String, detail: JsValue)(implicit
                                                                                          hc: HeaderCarrier
  ): ExtendedDataEvent = {
    val strPath = hc.otherHeaders.toMap.get("path")
    val strReferer = getReferer(hc)
    ExtendedDataEvent(
      auditSource = auditSource,
      auditType = auditType,
      tags = Map(
        "transactionName" -> Some(transactionName),
        "X-Session-ID" -> hc.sessionId.map(_.value),
        "X-Request-ID" -> hc.requestId.map(_.value),
        "clientIP" -> hc.trueClientIp,
        "clientPort" -> hc.trueClientPort,
        "deviceID" -> hc.deviceID,
        "path" -> strPath,
        "referer" -> Some(strReferer)
      ).map(x => x._2.map((x._1, _))).flatten.toMap,
      detail = detail
    )
  }

  private def buildChildRecordNumberUplift(url: String,
                                           upliftRequest: ChildRecordNumberUpliftRequest,
                                           upliftResponse: HttpResponse,
                                           journeyId: String):
  ChildRecordNumberUpliftAuditEvent = {
    ChildRecordNumberUpliftAuditEvent(
      journeyId,
      timestamp(),
      url,
      upliftRequest,
      upliftResponse.status.toString,
      upliftResponse.body
    )
  }

  private def timestamp(): String =
    java.time.Instant.now().atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_DATE_TIME)

  def childRecordNumberUplift(url: String,
                              request: ChildRecordNumberUpliftRequest,
                              response: HttpResponse,
                              auditType: String,
                              appName: String)(implicit hc: HeaderCarrier): ExtendedDataEvent = {
    buildDataEvent(auditType, s"$appName-$auditType",
      Json.toJson(buildChildRecordNumberUplift(url, request, response, auditType)))
  }

}
