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

import cats.data.EitherT
import connectors.FandFConnector
import helper.ApiPayloadHelper
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.*
import play.api.inject.bind
import play.api.inject.guice.{GuiceApplicationBuilder, GuiceableModule}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.*
import play.api.test.*
import play.api.test.Helpers.*
import play.api.{Application, Configuration, Environment}
import services.IndividualDetailsService
import uk.gov.hmrc.auth.core.*
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.v2.TrustedHelper
import uk.gov.hmrc.auth.core.retrieve.{Credentials, Retrieval, ~}
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}

import scala.concurrent.ExecutionContext.global
import scala.concurrent.{ExecutionContext, Future}

class IndividualsDetailsControllerSpec
    extends PlaySpec
    with Results
    with MockitoSugar
    with BeforeAndAfterEach
    with ApiPayloadHelper {

  implicit val hc: HeaderCarrier                = HeaderCarrier()
  implicit val ec: ExecutionContext             = global
  implicit val config: play.api.Configuration   = mock[Configuration]
  implicit val env: Environment                 = mock[Environment]
  implicit val cc: MessagesControllerComponents = mock[MessagesControllerComponents]

  private val testNino     = "AB123456Q"
  private val credentials  = Credentials("providerId", "providerType")
  private val resolveMerge = "Y"

  private val mockAuthConnector            = mock[AuthConnector]
  private val mockIndividualDetailsService = mock[IndividualDetailsService]
  private val mockFandFConnector           = mock[FandFConnector]

  val actionBuilder: ActionBuilder[Request, AnyContent] = DefaultActionBuilder(
    stubControllerComponents().parsers.defaultBodyParser
  )
  when(cc.actionBuilder).thenReturn(actionBuilder)

  val retrievalResult: Future[Option[String] ~ Option[CredentialRole] ~ Option[String] ~ Option[Credentials]] =
    Future.successful(new ~(new ~(new ~(Some(testNino), Some(User)), Some("id")), Some(credentials)))

  val modules: Seq[GuiceableModule] =
    Seq(
      bind[IndividualDetailsService].toInstance(mockIndividualDetailsService),
      bind[AuthConnector].toInstance(mockAuthConnector)
    )

  val application: Application = new GuiceApplicationBuilder()
    .configure(conf = "auditing.enabled" -> false, "metrics.enabled" -> false, "metrics.jvm" -> false)
    .overrides(modules: _*)
    .build()

  override def beforeEach(): Unit = {
    reset(mockAuthConnector, mockFandFConnector)
    when(
      mockAuthConnector.authorise[Option[String] ~ Option[CredentialRole] ~ Option[String] ~ Option[TrustedHelper]](
        any[Predicate],
        any[Retrieval[Option[String] ~ Option[CredentialRole] ~ Option[String] ~ Option[TrustedHelper]]]
      )(any[HeaderCarrier], any[ExecutionContext])
    ).thenReturn(retrievalResult)

    when(mockFandFConnector.getTrustedHelper()(any())).thenReturn(Future.successful(None))
    ()
  }

  private val validApiResponse: EitherT[Future, UpstreamErrorResponse, JsValue] = EitherT(
    Future.successful(Right(apiIndividualDetailsJsonOneNameOneAddress))
  )

  "IndividualsDetailsController" must {

    "return OK for getIndividualDetails" in {
      val controller             =
        new IndividualsDetailsController(mockAuthConnector, mockFandFConnector, mockIndividualDetailsService)
      when(mockIndividualDetailsService.getIndividualDetails(any, any, any)(any)).thenReturn(validApiResponse)
      val result: Future[Result] = controller.getIndividualDetails(testNino, resolveMerge).apply(FakeRequest())
      status(result) mustBe OK
      Json.parse(contentAsString(result)) mustBe apiTransformedIndividualDetailsJsonOneNameOneAddress
    }

    "return OK for getIndividualDetails when trusted helper user calls using helpee nino" in {

      val controller    =
        new IndividualsDetailsController(mockAuthConnector, mockFandFConnector, mockIndividualDetailsService)
      val trustedHelper = TrustedHelper("PrincipalName", "AttorneyName", "ReturnLink", Some("PrincipalNino"))

      when(mockFandFConnector.getTrustedHelper()(any())).thenReturn(Future.successful(Some(trustedHelper)))

      when(mockIndividualDetailsService.getIndividualDetails(any, any, any)(any)).thenReturn(validApiResponse)

      val result: Future[Result] =
        controller.getIndividualDetails(trustedHelper.principalNino.get, resolveMerge).apply(FakeRequest())
      status(result) mustBe OK
      Json.parse(contentAsString(result)) mustBe apiTransformedIndividualDetailsJsonOneNameOneAddress
    }

    "return Unauthorized for getIndividualDetails when user is not authorized" in {

      val controller =
        new IndividualsDetailsController(mockAuthConnector, mockFandFConnector, mockIndividualDetailsService)

      when(
        mockAuthConnector.authorise[Option[String] ~ Option[CredentialRole] ~ Option[String]](
          eqTo(AuthProviders(AuthProvider.GovernmentGateway)),
          any[Retrieval[Option[String] ~ Option[CredentialRole] ~ Option[String]]]
        )(any[HeaderCarrier], any[ExecutionContext])
      )
        .thenReturn(Future.failed(new InsufficientEnrolments))

      val result: Future[Result] = controller.getIndividualDetails(testNino, resolveMerge).apply(FakeRequest())
      status(result) mustBe UNAUTHORIZED
    }

    "return Unauthorized when NINO in authContext does not match the NINO in the request" in {
      val differentNino = "CD123456Q"
      val controller    =
        new IndividualsDetailsController(mockAuthConnector, mockFandFConnector, mockIndividualDetailsService)

      when(
        mockAuthConnector.authorise[Option[String] ~ Option[CredentialRole] ~ Option[String]](
          eqTo(AuthProviders(AuthProvider.GovernmentGateway)),
          any[Retrieval[Option[String] ~ Option[CredentialRole] ~ Option[String]]]
        )(any[HeaderCarrier], any[ExecutionContext])
      )
        .thenReturn(Future.successful(new ~(new ~(Some(testNino), Some(User)), Some("id"))))

      val result: Future[Result] = controller.getIndividualDetails(differentNino, resolveMerge).apply(FakeRequest())
      status(result) mustBe UNAUTHORIZED
    }
  }

  "return BAD_REQUEST for getIndividualDetails" in {
    val controller =
      new IndividualsDetailsController(mockAuthConnector, mockFandFConnector, mockIndividualDetailsService)

    val invalidApiResponse: EitherT[Future, UpstreamErrorResponse, JsValue] =
      EitherT(Future.successful(Left(UpstreamErrorResponse("", BAD_REQUEST))))

    when(mockIndividualDetailsService.getIndividualDetails(any, any, any)(any))
      .thenReturn(invalidApiResponse)

    val result: Future[Result] = controller.getIndividualDetails(testNino, resolveMerge).apply(FakeRequest())
    status(result) mustBe BAD_REQUEST
  }
  "return UNAUTHORIZED for getIndividualDetails" in {
    val controller =
      new IndividualsDetailsController(mockAuthConnector, mockFandFConnector, mockIndividualDetailsService)

    val invalidApiResponse: EitherT[Future, UpstreamErrorResponse, JsValue] =
      EitherT(Future.successful(Left(UpstreamErrorResponse("", UNAUTHORIZED))))

    when(mockIndividualDetailsService.getIndividualDetails(any, any, any)(any))
      .thenReturn(invalidApiResponse)

    val result: Future[Result] = controller.getIndividualDetails(testNino, resolveMerge).apply(FakeRequest())
    status(result) mustBe UNAUTHORIZED
  }
  "return NOT_FOUND for getIndividualDetails" in {
    val controller =
      new IndividualsDetailsController(mockAuthConnector, mockFandFConnector, mockIndividualDetailsService)

    val invalidApiResponse: EitherT[Future, UpstreamErrorResponse, JsValue] =
      EitherT(Future.successful(Left(UpstreamErrorResponse("", NOT_FOUND))))

    when(mockIndividualDetailsService.getIndividualDetails(any, any, any)(any))
      .thenReturn(invalidApiResponse)

    val result: Future[Result] = controller.getIndividualDetails(testNino, resolveMerge).apply(FakeRequest())
    status(result) mustBe NOT_FOUND
  }
  "return INTERNAL_SERVER_ERROR for getIndividualDetails" in {
    val controller =
      new IndividualsDetailsController(mockAuthConnector, mockFandFConnector, mockIndividualDetailsService)

    val invalidApiResponse: EitherT[Future, UpstreamErrorResponse, JsValue] =
      EitherT(Future.successful(Left(UpstreamErrorResponse("", INTERNAL_SERVER_ERROR))))

    when(mockIndividualDetailsService.getIndividualDetails(any, any, any)(any))
      .thenReturn(invalidApiResponse)

    val result: Future[Result] = controller.getIndividualDetails(testNino, resolveMerge).apply(FakeRequest())
    status(result) mustBe INTERNAL_SERVER_ERROR
  }
  "return NOT_IMPLEMENTED for getIndividualDetails" in {
    val controller =
      new IndividualsDetailsController(mockAuthConnector, mockFandFConnector, mockIndividualDetailsService)

    val invalidApiResponse: EitherT[Future, UpstreamErrorResponse, JsValue] =
      EitherT(Future.successful(Left(UpstreamErrorResponse("", NOT_IMPLEMENTED))))

    when(mockIndividualDetailsService.getIndividualDetails(any, any, any)(any))
      .thenReturn(invalidApiResponse)

    val result: Future[Result] = controller.getIndividualDetails(testNino, resolveMerge).apply(FakeRequest())
    status(result) mustBe NOT_IMPLEMENTED
  }
  "return other status for getIndividualDetails" in {
    val controller =
      new IndividualsDetailsController(mockAuthConnector, mockFandFConnector, mockIndividualDetailsService)

    val invalidApiResponse: EitherT[Future, UpstreamErrorResponse, JsValue] =
      EitherT(Future.successful(Left(UpstreamErrorResponse("", IM_A_TEAPOT))))

    when(mockIndividualDetailsService.getIndividualDetails(any, any, any)(any))
      .thenReturn(invalidApiResponse)

    val result: Future[Result] = controller.getIndividualDetails(testNino, resolveMerge).apply(FakeRequest())
    status(result) mustBe IM_A_TEAPOT
  }
}
