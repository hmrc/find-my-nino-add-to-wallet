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


import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import org.mockito.MockitoSugar
import org.scalatestplus.play._
import play.api.inject.bind
import play.api.inject.guice.{GuiceApplicationBuilder, GuiceableModule}
import play.api.mvc._
import play.api.test.Helpers._
import play.api.test._
import play.api.{Application, Configuration, Environment}
import services.IndividualDetailsService
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import scala.concurrent.ExecutionContext.global
import scala.concurrent.{ExecutionContext, Future}



class IndividualsDetailsControllerSpec extends PlaySpec with Results with MockitoSugar {

  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val ec: ExecutionContext = global
  implicit val config: play.api.Configuration = mock[Configuration]
  implicit val env:Environment = mock[Environment]
  implicit val cc: MessagesControllerComponents = mock[MessagesControllerComponents]

  val testNino  = "AB123456Q"
  val resolveMerge = "Y"

  private val mockAuthConnector = mock[AuthConnector]
  private val mockIndividualDetailsService = mock[IndividualDetailsService]

  val actionBuilder: ActionBuilder[Request, AnyContent] = DefaultActionBuilder(stubControllerComponents().parsers.defaultBodyParser)
  when(cc.actionBuilder).thenReturn(actionBuilder)

  val retrievalResult: Future[Option[String] ~ Option[CredentialRole] ~ Option[String]] =
    Future.successful(new ~(new ~(Some(testNino), Some(User)), Some("id")))

  when(
    mockAuthConnector.authorise[Option[String] ~ Option[CredentialRole] ~ Option[String]](
      eqTo(AuthProviders(AuthProvider.GovernmentGateway)),
      any[Retrieval[Option[String] ~ Option[CredentialRole] ~ Option[String]]])(any[HeaderCarrier], any[ExecutionContext]))
    .thenReturn(retrievalResult)

  val modules: Seq[GuiceableModule] =
    Seq(
      bind[IndividualDetailsService].toInstance(mockIndividualDetailsService),
      bind[AuthConnector].toInstance(mockAuthConnector)
    )

  val application: Application = new GuiceApplicationBuilder()
    .configure(conf = "auditing.enabled" -> false, "metrics.enabled" -> false, "metrics.jvm" -> false).
    overrides(modules: _*).build()

  "IndividualsDetailsController" must {

    "return OK for getIndividualDetails" in {

      val controller = new IndividualsDetailsController(mockAuthConnector, mockIndividualDetailsService)

      when(mockIndividualDetailsService.getIndividualDetails(any, any)(any))
        .thenReturn(Future.successful(HttpResponse(200, "")))

      val result: Future[Result] = controller.getIndividualDetails(testNino, resolveMerge).apply(FakeRequest())
      status(result) mustBe OK

    }

    "return Unauthorized for getIndividualDetails when user is not authorized" in {

      val controller = new IndividualsDetailsController(mockAuthConnector, mockIndividualDetailsService)

      when(
        mockAuthConnector.authorise[Option[String] ~ Option[CredentialRole] ~ Option[String]](
          eqTo(AuthProviders(AuthProvider.GovernmentGateway)),
          any[Retrieval[Option[String] ~ Option[CredentialRole] ~ Option[String]]])(any[HeaderCarrier], any[ExecutionContext]))
        .thenReturn(Future.failed(new InsufficientEnrolments))

      val result: Future[Result] = controller.getIndividualDetails(testNino, resolveMerge).apply(FakeRequest())
      status(result) mustBe UNAUTHORIZED
    }

    "return Unauthorized when NINO in authContext does not match the NINO in the request" in {
      val differentNino = "CD123456Q"
      val controller = new IndividualsDetailsController(mockAuthConnector, mockIndividualDetailsService)

      when(
        mockAuthConnector.authorise[Option[String] ~ Option[CredentialRole] ~ Option[String]](
          eqTo(AuthProviders(AuthProvider.GovernmentGateway)),
          any[Retrieval[Option[String] ~ Option[CredentialRole] ~ Option[String]]])(any[HeaderCarrier], any[ExecutionContext]))
        .thenReturn(Future.successful(new ~(new ~(Some(testNino), Some(User)), Some("id"))))

      val result: Future[Result] = controller.getIndividualDetails(differentNino, resolveMerge).apply(FakeRequest())
      status(result) mustBe UNAUTHORIZED
    }
  }
}

