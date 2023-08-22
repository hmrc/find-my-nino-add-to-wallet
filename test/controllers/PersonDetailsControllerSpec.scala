/*
 * Copyright 2023 HM Revenue & Customs
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

import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar
import org.mockito.MockitoSugar.{mock, when}
import org.scalatest.BeforeAndAfter
import org.scalatest.concurrent.ScalaFutures.whenReady
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.Application
import play.api.http.Status.OK
import play.api.inject.bind
import play.api.inject.guice.{GuiceApplicationBuilder, GuiceableModule}
import play.api.libs.json.{JsObject, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.PersonDetailsService
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}
import uk.gov.hmrc.auth.core.{AuthConnector, CredentialRole, User}
import uk.gov.hmrc.http.HeaderCarrier

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

class PersonDetailsControllerSpec extends AnyWordSpec with Matchers with MockitoSugar with BeforeAndAfter {

  import PersonDetailsControllerSpec._

  "createPersonDetails" must {
    "should return OK with the pd" in {
      when(mockPersonDetailsService.createPersonDetails(
        any(),
        any(),
        any(),
        any())(any()))
        .thenReturn(Right("pdId"))

      val result = controller.createPersonDetailsRow()(fakeRequestWithAuth.withJsonBody(Json.parse(fakePersonDetailsJson)))

      whenReady(result) { _ =>
        status(result) mustBe OK
        contentAsString(result) mustBe "pdId"
      }
    }

    "should return InternalServerError when failure occurs" in {
      when(mockPersonDetailsService.createPersonDetails(
        any(),
        any(),
        any(),
        any())(any()))
        .thenReturn(Left(new Exception("failed")))

      val result = controller.createPersonDetailsRow()(fakeRequestWithAuth.withJsonBody(Json.parse(fakePersonDetailsJson)))

      whenReady(result) { _ =>
        status(result) mustBe 500
      }
    }
  }

  "getPersonDetails" must {
    "should return OK with the pd" in {
      when(mockPersonDetailsService.getPersonDetailsById(any())(any()))
        .thenReturn(Future successful Some("{}"))

      val result = controller.getPersonDetails("pdId")(fakeRequestWithAuth)

      whenReady(result) { _ =>
        status(result) mustBe OK
        contentAsString(result) mustBe "\"{}\""
      }
    }
  }

}

object PersonDetailsControllerSpec {
  implicit val hc: HeaderCarrier = HeaderCarrier()
  private val passId = UUID.randomUUID().toString

  private val postBody: JsObject = Json.obj("fullName" -> "TestName TestSurname", "nino" -> "AB 12 34 56 Q",
    "personDetails" -> "", "expirationDate" -> "")

  private val fakeRequestWithAuth = FakeRequest("GET", "/").withHeaders(
    ("Content-Type" -> "application/json"),
    ("Authorization" -> "Bearer 123"))

  private val fakePostRequestWithAuth = FakeRequest("POST", "/").withHeaders(
    ("Content-Type" -> "application/json"),
    ("Authorization" -> "Bearer 123"))

  private val mockPersonDetailsService = mock[PersonDetailsService]
  private val mockAuthConnector = mock[AuthConnector]

  val retrievalResult: Future[Option[String] ~ Option[CredentialRole] ~ Option[String]] =
    Future.successful(new~(new~(Some("nino"), Some(User)), Some("id")))

  when(
    mockAuthConnector.authorise[Option[String] ~ Option[CredentialRole] ~ Option[String]](
      any[Predicate],
      any[Retrieval[Option[String] ~ Option[CredentialRole] ~ Option[String]]])(any[HeaderCarrier], any[ExecutionContext]))
    .thenReturn(retrievalResult)


  val modules: Seq[GuiceableModule] =
    Seq(
      bind[PersonDetailsService].toInstance(mockPersonDetailsService),
      bind[AuthConnector].toInstance(mockAuthConnector)
    )

  val application: Application = new GuiceApplicationBuilder()
    .configure(conf = "auditing.enabled" -> false, "metrics.enabled" -> false, "metrics.jvm" -> false).
    overrides(modules: _*).build()
  private val controller = application.injector.instanceOf[PersonDetailsController]

  private val fakePersonDetailsJson =
    """
      |{
      |"person":{"firstName":"BOB","lastName":"JONES","title":"Mrs","sex":"F","dateOfBirth":"1962-09-08","nino":"AA000003B"},
      |"address":{"line1":"11 Test Street","line2":"Testtown","postcode":"FX97 4TU","country":"GREAT BRITAIN","startDate":"2016-04-06","type":"Residential"},
      |"correspondenceAddress":{"line1":"11 Test Street","line2":"Testtown","postcode":"FX97 4TU","country":"GREAT BRITAIN","startDate":"2012-07-05","type":"Correspondence"}
      |}
      |""".stripMargin

}

