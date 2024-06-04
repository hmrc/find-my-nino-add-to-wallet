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

package connectors

import config.AppConfig
import models.CorrelationId
import models.nps.ChildReferenceNumberUpliftRequest
import org.mockito.MockitoSugar
import play.api.Application
import play.api.libs.json.Json
import play.api.test.{DefaultAwaitTimeout, Injecting}
import services.AuditService
import uk.gov.hmrc.http.client.HttpClientV2
import util.{WireMockHelper, WiremockStub}

import java.util.UUID

class NPSFMNConnectorSpec
  extends WiremockStub
    with WireMockHelper
    with MockitoSugar
    with DefaultAwaitTimeout
    with Injecting {

  override implicit lazy val app: Application = app(
    Map("microservice.services.nps-crn-api.port" -> server.port(),
    )
  )

  val ninoWithoutSuffix = "XX000000"

  val jsonUnprocessableEntity =
    s"""
       |{
       |  "failures": [
       |    {
       |      "reason": "Date of birth does not match",
       |      "code": "63484"
       |    }
       |  ]
       |}
       |""".stripMargin

  val jsonForbidden =
    s"""
       |{
       |  "reason": "Forbidden",
       |  "code": "403.2"
       |}
       |""".stripMargin

  val jsonBadRequest =
    s"""
       |{
       |  "failures": [
       |    {
       |      "reason": "HTTP message not readable",
       |      "code": "400.2"
       |    },
       |    {
       |      "reason": "Constraint Violation - Invalid/Missing input parameter",
       |      "code": "400.1"
       |    }
       |  ]
       |}
       |""".stripMargin


  trait SpecSetup {

    def url(nino: String): String

    lazy val connector = {
      val httpClient2 = app.injector.instanceOf[HttpClientV2]
      val config = app.injector.instanceOf[AppConfig]
      val auditService = app.injector.instanceOf[AuditService]
      new NPSConnector(httpClient2, config, auditService)
    }
  }

  "NPS Connector" must {

    trait LocalSetup extends SpecSetup {
      def url(ninoWithoutSuffix: String) = s"/nps/nps-json-service/nps/v1/api/individual/${ninoWithoutSuffix}/adult-registration"
      val body = mock[ChildReferenceNumberUpliftRequest]
    }

    "return 204 NO_CONTENT when called with a CRN" in new LocalSetup {
      stubPut(url(ninoWithoutSuffix), NO_CONTENT, Some(Json.toJson(body).toString()), Some(""))
      val result = connector.upliftCRN(ninoWithoutSuffix, body).futureValue.leftSideValue
      result.status mustBe NO_CONTENT
      result.body mustBe ""
    }

    "return 400 BAD_REQUEST when called with invalid request object" in new LocalSetup {
      stubPut(url(ninoWithoutSuffix), BAD_REQUEST, Some(Json.toJson(body).toString()), Some(jsonBadRequest))
      val result = connector.upliftCRN(ninoWithoutSuffix, body).futureValue.leftSideValue
      result.status mustBe BAD_REQUEST
      result.body mustBe jsonBadRequest
    }

    "return 403 FORBIDDEN when called with forbidden request" in new LocalSetup {
      stubPut(url(ninoWithoutSuffix), FORBIDDEN, Some(Json.toJson(body).toString()), Some(jsonForbidden))
      val result = connector.upliftCRN(ninoWithoutSuffix, body).futureValue.leftSideValue
      result.status mustBe FORBIDDEN
      result.body mustBe jsonForbidden
    }

    "return 422 UNPROCESSABLE_ENTITY when the action cannot be completed" in new LocalSetup {
      stubPut(url(ninoWithoutSuffix), UNPROCESSABLE_ENTITY, Some(Json.toJson(body).toString()), Some(jsonUnprocessableEntity))
      val result = connector.upliftCRN(ninoWithoutSuffix, body).futureValue.leftSideValue
      result.status mustBe UNPROCESSABLE_ENTITY
      result.body mustBe jsonUnprocessableEntity
    }

    "return 404 NOT_FOUND when resource cannot be found" in new LocalSetup {
      stubPut(url(ninoWithoutSuffix), NOT_FOUND, Some(Json.toJson(body).toString()), None)
      val result = connector.upliftCRN(ninoWithoutSuffix, body).futureValue.leftSideValue
      result.status mustBe NOT_FOUND
      result.body mustBe ""
    }

    "return 500 INTERNAL_SERVER_ERROR when exception is thrown" in new LocalSetup {
      stubPut(url(ninoWithoutSuffix), INTERNAL_SERVER_ERROR, Some(Json.toJson(body).toString()), None)
      val result = connector.upliftCRN(ninoWithoutSuffix, body).futureValue.leftSideValue
      result.status mustBe INTERNAL_SERVER_ERROR
      result.body mustBe ""
    }
  }
}
