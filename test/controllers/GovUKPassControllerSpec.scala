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
import org.scalatest.BeforeAndAfter
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{JsObject, Json}
import org.scalatest.concurrent.ScalaFutures.whenReady
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.inject.bind
import services.GovUKPassService
import uk.gov.hmrc.auth.core.{AuthConnector, CredentialRole, User}
import uk.gov.hmrc.http.HeaderCarrier
import play.api.Application
import play.api.inject.guice.{GuiceApplicationBuilder, GuiceableModule}
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

class GovUKPassControllerSpec extends AnyWordSpec with Matchers with MockitoSugar with BeforeAndAfter {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  //private val passId = UUID.randomUUID().toString
  private val govUrl = "govUrl"
  private val qrCodeString = "qrCodeString"
  private val createPassRequest: JsObject = Json.obj(
    "givenName" -> "TestGivenName",
    "familyName" -> "TestSurname",
    "nino" -> "AB 12 34 56 Q"
  )

  private val fakeRequestWithAuth = FakeRequest("GET", "/").withHeaders(
    ("Content-Type" -> "application/json"),
    ("Authorization" -> "Bearer 123"))

  val mockAuthConnector: AuthConnector = mock[AuthConnector]
  val mockGovUKPassService: GovUKPassService = mock[GovUKPassService]

  val retrievalResult: Future[Option[String] ~ Option[CredentialRole] ~ Option[String]] =
    Future.successful(new~(new~(Some("nino"), Some(User)), Some("id")))

  when(
    mockAuthConnector.authorise[Option[String] ~ Option[CredentialRole] ~ Option[String]](
      any[Predicate],
      any[Retrieval[Option[String] ~ Option[CredentialRole] ~ Option[String]]])(any[HeaderCarrier], any[ExecutionContext]))
    .thenReturn(retrievalResult)

  val modules: Seq[GuiceableModule] =
    Seq(
      bind[GovUKPassService].toInstance(mockGovUKPassService),
      bind[AuthConnector].toInstance(mockAuthConnector)
    )

  val application: Application = new GuiceApplicationBuilder()
    .configure(conf = "auditing.enabled" -> false, "metrics.enabled" -> false, "metrics.jvm" -> false).
    overrides(modules: _*).build()

  private val controller = application.injector.instanceOf[GovUKPassController]

  "createGovUKPass" must {
    "should return OK with the details of pass" in {
      when(mockGovUKPassService.createGovUKPass(any(), any(), any())(any())).thenReturn(Right(govUrl, qrCodeString))

      val result = controller.createGovUKPass()(fakeRequestWithAuth.withJsonBody(createPassRequest))

      whenReady(result) { _ =>
        status(result) mustBe OK
      }
    }
  }

}
