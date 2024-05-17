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

import models.nps.CRNUpliftRequest
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import org.mockito.MockitoSugar
import org.scalatest.concurrent.ScalaFutures.whenReady
import org.scalatestplus.play._
import play.api.inject.bind
import play.api.inject.guice.{GuiceApplicationBuilder, GuiceableModule}
import play.api.libs.json.Json
import play.api.mvc._
import play.api.test.Helpers._
import play.api.test._
import play.api.{Application, Configuration, Environment}
import services.NPSService
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import scala.concurrent.ExecutionContext.global
import scala.concurrent.{ExecutionContext, Future}


class NpsControllerSpec extends PlaySpec with Results with MockitoSugar {

  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val ec: ExecutionContext = global
  implicit val config: play.api.Configuration = mock[Configuration]
  implicit val env:Environment = mock[Environment]
  implicit val cc: MessagesControllerComponents = mock[MessagesControllerComponents]

  val identifier  = "AB123456Q"

  private val mockAuthConnector = mock[AuthConnector]
  private val mockNPSService = mock[NPSService]

  val actionBuilder: ActionBuilder[Request, AnyContent] = DefaultActionBuilder(stubControllerComponents().parsers.defaultBodyParser)
  when(cc.actionBuilder).thenReturn(actionBuilder)

  val retrievalResult: Future[Option[String] ~ Option[CredentialRole] ~ Option[String]] =
    Future.successful(new ~(new ~(Some(identifier), Some(User)), Some("id")))

  when(
    mockAuthConnector.authorise[Option[String] ~ Option[CredentialRole] ~ Option[String]](
      eqTo(AuthProviders(AuthProvider.GovernmentGateway)),
      any[Retrieval[Option[String] ~ Option[CredentialRole] ~ Option[String]]])(any[HeaderCarrier], any[ExecutionContext]))
    .thenReturn(retrievalResult)

  val modules: Seq[GuiceableModule] =
    Seq(
      bind[NPSService].toInstance(mockNPSService),
      bind[AuthConnector].toInstance(mockAuthConnector)
    )

  val application: Application = new GuiceApplicationBuilder()
    .configure(conf = "auditing.enabled" -> false, "metrics.enabled" -> false, "metrics.jvm" -> false).
    overrides(modules: _*).build()

  private val fakeRequestWithAuth = FakeRequest("PUT", "/").withHeaders(
    ("Content-Type" -> "application/json"),
    ("Authorization" -> "Bearer 123"))

  val controller: NpsController = application.injector.instanceOf[NpsController]

  "MyNpsController" must {

    "return NO CONTENT (204) for upliftCRN" in {

      val crnUpliftRequest = CRNUpliftRequest("test", "test", "test")

      when(mockNPSService.upliftCRN(any, any)(any))
        .thenReturn(Future.successful(HttpResponse(NO_CONTENT, "")))

      val result = controller.upliftCRN(identifier)(fakeRequestWithAuth.withJsonBody(Json.toJson(crnUpliftRequest)))

      whenReady(result) { _ =>
        status(result) mustBe NO_CONTENT
      }
    }

    "return BAD REQUEST (400) for upliftCRN" in {

      val crnUpliftRequest = CRNUpliftRequest("test", "test", "test")

      when(mockNPSService.upliftCRN(any, any)(any))
        .thenReturn(Future.successful(HttpResponse(BAD_REQUEST, "Bad Request")))

      val result = controller.upliftCRN(identifier)(fakeRequestWithAuth.withJsonBody(Json.toJson(crnUpliftRequest)))

      whenReady(result) { _ =>
        status(result) mustBe BAD_REQUEST
      }
    }
  }
}

