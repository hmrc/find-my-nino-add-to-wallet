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

package controllers

import connectors.NPSConnector
import models.CorrelationId
import models.nps.CRNUpliftRequest
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfter
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.MessagesApi
import play.api.mvc.ControllerComponents
import play.api.test.FakeRequest
import play.api.libs.json.Json
import uk.gov.hmrc.http.HeaderCarrier
import play.api.test.Helpers._
import services.NPSService
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}
import uk.gov.hmrc.auth.core.{AuthConnector, AuthProvider, AuthProviders, CredentialRole, User}
import play.api.inject.bind
import play.api.inject.guice.{GuiceApplicationBuilder, GuiceableModule}
import play.api.Application

import scala.concurrent.{ExecutionContext, Future}

class NPSControllerSpec extends AnyWordSpec with Matchers with MockitoSugar with BeforeAndAfter {

  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val correlationId: CorrelationId = CorrelationId.random
  implicit val ec: ExecutionContext = ExecutionContext.global
  val nino = "AA000003B"

  private val fakeRequestWithAuth = FakeRequest("GET", "/").withHeaders(
    ("Content-Type" -> "application/json"),
    ("Authorization" -> "Bearer 123")
  )

  private val mockMessagesApi = mock[MessagesApi]
  private val mockControllerComponents = mock[ControllerComponents]
  private val mockAuthConnector = mock[AuthConnector]
  private val mockNPSService = new NPSService(mock[NPSConnector])


  val retrievalResult: Future[Option[String] ~ Option[CredentialRole] ~ Option[String]] =
    Future.successful(new~(new~(Some(nino), Some(User)), Some("id")))

  when(
    mockAuthConnector.authorise[Option[String] ~ Option[CredentialRole] ~ Option[String]](
      eqTo(AuthProviders(AuthProvider.GovernmentGateway)),
      any[Retrieval[Option[String] ~ Option[CredentialRole] ~ Option[String]]])(any[HeaderCarrier], any[ExecutionContext]))
    .thenReturn(retrievalResult)

  val modules: Seq[GuiceableModule] =
    Seq(
      bind[MessagesApi].toInstance(mockMessagesApi),
      bind[ControllerComponents].toInstance(mockControllerComponents),
      bind[AuthConnector].toInstance(mockAuthConnector),
      bind[NPSService].toInstance(mockNPSService)
    )

  val application: Application = new GuiceApplicationBuilder()
    .configure(conf = "auditing.enabled" -> false, "metrics.enabled" -> false, "metrics.jvm" -> false).
    overrides(modules: _*).build()

  private val controller = application.injector.instanceOf[NPSController]

  "upliftCRN" must {
    "return 204 No Content when the service returns a 204 response" in {
      val crnUpliftRequest = CRNUpliftRequest("test", "test", "test")
      val httpResponse = uk.gov.hmrc.http.HttpResponse(NO_CONTENT, "No Content")

      when(mockNPSService.upliftCRN(eqTo(nino), eqTo(CRNUpliftRequest(any(),any(),any()))))
        .thenReturn(Future.successful(httpResponse))

      val result = controller.upliftCRN(nino)(fakeRequestWithAuth.withBody(Json.toJson(crnUpliftRequest)))

      status(result) mustBe NO_CONTENT
    }

    "return 400 Bad Request when the service returns a 400 response" in {
      val crnUpliftRequest = CRNUpliftRequest("test", "test", "test")
      val httpResponse = uk.gov.hmrc.http.HttpResponse(BAD_REQUEST, "Bad Request")

      when(mockNPSService.upliftCRN(eqTo(nino), eqTo(CRNUpliftRequest(any(), any(), any()))))
        .thenReturn(Future.successful(httpResponse))

      val result = controller.upliftCRN(nino)(fakeRequestWithAuth.withBody(Json.toJson(crnUpliftRequest)))

      status(result) mustBe BAD_REQUEST
    }

    "return 403 Forbidden when the service returns a 403 response" in {
      val crnUpliftRequest = CRNUpliftRequest("test", "test", "test")
      val httpResponse = uk.gov.hmrc.http.HttpResponse(FORBIDDEN, "Forbidden")

      when(mockNPSService.upliftCRN(eqTo(nino), eqTo(CRNUpliftRequest(any(), any(), any()))))
        .thenReturn(Future.successful(httpResponse))

      val result = controller.upliftCRN(nino)(fakeRequestWithAuth.withBody(Json.toJson(crnUpliftRequest)))

      status(result) mustBe FORBIDDEN
    }

    "return 422 Unprocessable Entity when the service returns a 422 response" in {
      val crnUpliftRequest = CRNUpliftRequest("test", "test", "test")
      val httpResponse = uk.gov.hmrc.http.HttpResponse(UNPROCESSABLE_ENTITY, "Unprocessable Entity")

      when(mockNPSService.upliftCRN(eqTo(nino), eqTo(CRNUpliftRequest(any(), any(), any()))))
        .thenReturn(Future.successful(httpResponse))

      val result = controller.upliftCRN(nino)(fakeRequestWithAuth.withBody(Json.toJson(crnUpliftRequest)))

      status(result) mustBe UNPROCESSABLE_ENTITY
    }

    "return 404 Not Found when the service returns a 404 response" in {
      val crnUpliftRequest = CRNUpliftRequest("test", "test", "test")
      val httpResponse = uk.gov.hmrc.http.HttpResponse(NOT_FOUND, "Not Found")

      when(mockNPSService.upliftCRN(eqTo(nino), eqTo(CRNUpliftRequest(any(), any(), any()))))
        .thenReturn(Future.successful(httpResponse))

      val result = controller.upliftCRN(nino)(fakeRequestWithAuth.withBody(Json.toJson(crnUpliftRequest)))

      status(result) mustBe NOT_FOUND
    }

    "return 500 Internal Server Error when the service returns a 204 response" in {
      val crnUpliftRequest = CRNUpliftRequest("test", "test", "test")
      val httpResponse = uk.gov.hmrc.http.HttpResponse(INTERNAL_SERVER_ERROR, "Internal Server Error")

      when(mockNPSService.upliftCRN(eqTo(nino), eqTo(CRNUpliftRequest(any(), any(), any()))))
        .thenReturn(Future.successful(httpResponse))

      val result = controller.upliftCRN(nino)(fakeRequestWithAuth.withBody(Json.toJson(crnUpliftRequest)))

      status(result) mustBe INTERNAL_SERVER_ERROR
    }

    "return the same status and body as the service response when the status code is not explicitly handled" in {
      val crnUpliftRequest = CRNUpliftRequest("test", "test", "test")
      val httpResponse = uk.gov.hmrc.http.HttpResponse(IM_A_TEAPOT, "Teapot")

      when(mockNPSService.upliftCRN(eqTo(nino), eqTo(CRNUpliftRequest(any(), any(), any()))))
        .thenReturn(Future.successful(httpResponse))

      val result = controller.upliftCRN(nino)(fakeRequestWithAuth.withBody(Json.toJson(crnUpliftRequest)))

      status(result) mustBe IM_A_TEAPOT
    }
  }
}