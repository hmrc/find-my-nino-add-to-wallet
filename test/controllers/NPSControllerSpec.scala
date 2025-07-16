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

package controllers

import connectors.FandFConnector
import models.nps.ChildReferenceNumberUpliftRequest
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import org.scalatest.concurrent.ScalaFutures.whenReady
import org.scalatestplus.play.*
import play.api.inject.bind
import play.api.inject.guice.{GuiceApplicationBuilder, GuiceableModule}
import play.api.libs.json.Json
import play.api.mvc.*
import play.api.test.Helpers.*
import play.api.test.*
import play.api.{Application, Configuration, Environment}
import services.NPSService
import uk.gov.hmrc.auth.core.*
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import scala.concurrent.ExecutionContext.global
import scala.concurrent.{ExecutionContext, Future}

class NPSControllerSpec extends PlaySpec with Results with MockitoSugar {

  implicit val hc: HeaderCarrier                = HeaderCarrier()
  implicit val ec: ExecutionContext             = global
  implicit val config: play.api.Configuration   = mock[Configuration]
  implicit val env: Environment                 = mock[Environment]
  implicit val cc: MessagesControllerComponents = mock[MessagesControllerComponents]

  val identifier = "AB123456Q"

  private val mockAuthConnector  = mock[AuthConnector]
  private val mockNPSService     = mock[NPSService]
  private val mockFandFConnector = mock[FandFConnector]

  val actionBuilder: ActionBuilder[Request, AnyContent] = DefaultActionBuilder(
    stubControllerComponents().parsers.defaultBodyParser
  )
  when(cc.actionBuilder).thenReturn(actionBuilder)

  val retrievalResult: Future[Option[String] ~ Option[CredentialRole] ~ Option[String]] =
    Future.successful(new ~(new ~(Some(identifier), Some(User)), Some("id")))

  when(
    mockAuthConnector.authorise[Option[String] ~ Option[CredentialRole] ~ Option[String]](
      eqTo(AuthProviders(AuthProvider.GovernmentGateway)),
      any[Retrieval[Option[String] ~ Option[CredentialRole] ~ Option[String]]]
    )(any[HeaderCarrier], any[ExecutionContext])
  )
    .thenReturn(retrievalResult)

  when(mockFandFConnector.getTrustedHelper()(any())).thenReturn(Future.successful(None))

  val modules: Seq[GuiceableModule] =
    Seq(
      bind[NPSService].toInstance(mockNPSService),
      bind[AuthConnector].toInstance(mockAuthConnector),
      bind[FandFConnector].toInstance(mockFandFConnector)
    )

  val application: Application = new GuiceApplicationBuilder()
    .configure(conf = "auditing.enabled" -> false, "metrics.enabled" -> false, "metrics.jvm" -> false)
    .overrides(modules: _*)
    .build()

  private val fakeRequestWithAuth =
    FakeRequest("PUT", "/").withHeaders("Content-Type" -> "application/json", "Authorization" -> "Bearer 123")

  val controller: NPSController = application.injector.instanceOf[NPSController]

  "MyNpsController" must {

    "return NO CONTENT (204) for upliftCRN" in {

      val crnUpliftRequest = ChildReferenceNumberUpliftRequest("test", "test", "test")

      when(mockNPSService.upliftCRN(any, any)(any))
        .thenReturn(Future.successful(HttpResponse(NO_CONTENT, "")))

      val result = controller.upliftCRN(identifier)(fakeRequestWithAuth.withJsonBody(Json.toJson(crnUpliftRequest)))

      whenReady(result) { _ =>
        status(result) mustBe NO_CONTENT
      }
    }

    "return BAD REQUEST (400) for upliftCRN" in {

      val crnUpliftRequest = ChildReferenceNumberUpliftRequest("test", "test", "test")

      when(mockNPSService.upliftCRN(any, any)(any))
        .thenReturn(Future.successful(HttpResponse(BAD_REQUEST, "Bad Request")))

      val result = controller.upliftCRN(identifier)(fakeRequestWithAuth.withJsonBody(Json.toJson(crnUpliftRequest)))

      whenReady(result) { _ =>
        status(result) mustBe BAD_REQUEST
      }
    }

    "return FORBIDDEN (403) for upliftCRN" in {

      val crnUpliftRequest = ChildReferenceNumberUpliftRequest("test", "test", "test")

      when(mockNPSService.upliftCRN(any, any)(any))
        .thenReturn(Future.successful(HttpResponse(FORBIDDEN, "Forbidden")))

      val result = controller.upliftCRN(identifier)(fakeRequestWithAuth.withJsonBody(Json.toJson(crnUpliftRequest)))

      whenReady(result) { _ =>
        status(result) mustBe FORBIDDEN
      }
    }

    "return UNPROCESSABLE ENTITY (422) for upliftCRN" in {

      val crnUpliftRequest = ChildReferenceNumberUpliftRequest("test", "test", "test")

      when(mockNPSService.upliftCRN(any, any)(any))
        .thenReturn(Future.successful(HttpResponse(UNPROCESSABLE_ENTITY, "Unprocessable Entity")))

      val result = controller.upliftCRN(identifier)(fakeRequestWithAuth.withJsonBody(Json.toJson(crnUpliftRequest)))

      whenReady(result) { _ =>
        status(result) mustBe UNPROCESSABLE_ENTITY
      }
    }

    "return NOT FOUND (404) for upliftCRN" in {

      val crnUpliftRequest = ChildReferenceNumberUpliftRequest("test", "test", "test")

      when(mockNPSService.upliftCRN(any, any)(any))
        .thenReturn(Future.successful(HttpResponse(NOT_FOUND, "Not Found")))

      val result = controller.upliftCRN(identifier)(fakeRequestWithAuth.withJsonBody(Json.toJson(crnUpliftRequest)))

      whenReady(result) { _ =>
        status(result) mustBe NOT_FOUND
      }
    }

    "return INTERNAL SERVER ERROR (400) for upliftCRN" in {

      val crnUpliftRequest = ChildReferenceNumberUpliftRequest("test", "test", "test")

      when(mockNPSService.upliftCRN(any, any)(any))
        .thenReturn(Future.successful(HttpResponse(INTERNAL_SERVER_ERROR, "Internal Server Error")))

      val result = controller.upliftCRN(identifier)(fakeRequestWithAuth.withJsonBody(Json.toJson(crnUpliftRequest)))

      whenReady(result) { _ =>
        status(result) mustBe INTERNAL_SERVER_ERROR
      }
    }

    "return the same status and body as the service response when the status code is not explicitly handled" in {

      val crnUpliftRequest = ChildReferenceNumberUpliftRequest("test", "test", "test")

      when(mockNPSService.upliftCRN(any, any)(any))
        .thenReturn(Future.successful(HttpResponse(IM_A_TEAPOT, "Teapot")))

      val result = controller.upliftCRN(identifier)(fakeRequestWithAuth.withJsonBody(Json.toJson(crnUpliftRequest)))

      whenReady(result) { _ =>
        status(result) mustBe IM_A_TEAPOT
      }
    }
  }
}
