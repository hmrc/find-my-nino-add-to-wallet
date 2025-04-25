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

import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{reset, when}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatest.BeforeAndAfter
import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.concurrent.ScalaFutures.whenReady
import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.should.Matchers.should
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.Application
import play.api.http.Status.OK
import play.api.inject.bind
import play.api.inject.guice.{GuiceApplicationBuilder, GuiceableModule}
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.GooglePassService
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.v2.TrustedHelper
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}
import uk.gov.hmrc.auth.core.{AuthConnector, CredentialRole, User}
import uk.gov.hmrc.http.HeaderCarrier

import java.util.UUID
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}

class GooglePassControllerSpec extends AnyWordSpec with Matchers with MockitoSugar with BeforeAndAfter {

  import GooglePassControllerSpec._

  // setup before each test
  before {
    reset(mockAuthConnector, mockGooglePassService)

    val retrievalResult: Future[Option[String] ~ Option[CredentialRole] ~ Option[String] ~ Option[TrustedHelper]] =
      Future.successful(new ~(new ~(new ~(Some("AB123456Q"), Some(User)), Some("id")), None))

    when(
      mockAuthConnector.authorise[Option[String] ~ Option[CredentialRole] ~ Option[String] ~ Option[TrustedHelper]](
        any[Predicate],
        any[Retrieval[Option[String] ~ Option[CredentialRole] ~ Option[String] ~ Option[TrustedHelper]]]
      )(any[HeaderCarrier], any[ExecutionContext])
    )
      .thenReturn(retrievalResult)
  }

  "getPassCardByPassId" must {
    "return OK with the byte data of pass" in {
      when(mockGooglePassService.getPassUrlByPassIdAndNINO(eqTo(passId), eqTo("AB123456Q"))(any()))
        .thenReturn(Future.successful(Some("SomePassCodeData")))

      val result = controller.getPassUrlByPassId(passId)(fakeRequestWithAuth)

      whenReady(result) { _ =>
        status(result) mustBe OK
        contentAsBytes(result).length should be > 1
      }
    }

    "return NotFound when there is no record for given passId" in {
      when(mockGooglePassService.getPassUrlByPassIdAndNINO(eqTo(passId), eqTo("AB123456Q"))(any()))
        .thenReturn(Future.successful(None))

      val result = controller.getPassUrlByPassId(passId)(fakeRequestWithAuth)

      whenReady(result) { _ =>
        status(result) mustBe NOT_FOUND
      }
    }
  }

  "getQrCodeByPassId" must {
    "return OK with the byte data of qr code" in {
      when(mockGooglePassService.getQrCodeByPassIdAndNINO(eqTo(passId), eqTo("AB123456Q"))(any()))
        .thenReturn(Future.successful(Some("SomeQrCodeData".getBytes())))

      val result = controller.getQrCodeByPassId(passId)(fakeRequestWithAuth)

      whenReady(result) { _ =>
        status(result) mustBe OK
        contentAsBytes(result).length should be > 1
      }
    }

    "return Unauthorised with when the session NINO does not match Pass NINO" in {

      val retrievalResult: Future[Option[String] ~ Option[CredentialRole] ~ Option[String]] =
        Future.successful(new ~(new ~(Some("AB123456N"), Some(User)), Some("id")))

      when(
        mockAuthConnector.authorise[Option[String] ~ Option[CredentialRole] ~ Option[String]](
          any[Predicate],
          any[Retrieval[Option[String] ~ Option[CredentialRole] ~ Option[String]]]
        )(any[HeaderCarrier], any[ExecutionContext])
      )
        .thenReturn(retrievalResult)

      when(mockGooglePassService.getQrCodeByPassIdAndNINO(eqTo(passId), eqTo("AB123456Q"))(any()))
        .thenReturn(Future.successful(Some("SomeQrCodeData".getBytes())))

      val result = controller.getQrCodeByPassId(passId)(fakeRequestWithAuth)

      whenReady(result) { _ =>
        status(result) mustBe UNAUTHORIZED
      }
    }

    "return NotFound when there is no record for given passId" in {
      when(mockGooglePassService.getQrCodeByPassIdAndNINO(eqTo(passId), eqTo("AB123456Q"))(any()))
        .thenReturn(Future.successful(None))

      val result = controller.getQrCodeByPassId(passId)(fakeRequestWithAuth)

      whenReady(result) { _ =>
        status(result) mustBe NOT_FOUND
      }
    }
  }

  "createPassWithCredentials" must {
    "return OK with the uuid of the pass" ignore {
      when(mockGooglePassService.createPassWithCredentials(any(), any(), any(), any())(any()))
        .thenReturn(Right(passId))

      val result = controller.createPassWithCredentials()(
        fakeRequestWithAuth.withJsonBody(
          Json.obj("fullName" -> "TestName TestSurname", "nino" -> "AB 12 34 56 Q", "credentials" -> "xxxxxxx")
        )
      )

      whenReady(result, Timeout(1.second)) { _ =>
        status(result) mustBe OK
        contentAsString(result) mustBe passId
      }
    }
    "return InternalServerError when request body is invalid" ignore {
      when(mockGooglePassService.createPassWithCredentials(any(), any(), any(), any())(any()))
        .thenReturn(Left(new Exception("SomeError")))

      val result = controller.createPassWithCredentials()(
        fakeRequestWithAuth.withJsonBody(
          Json.obj("fullName" -> "TestName TestSurname", "nino" -> "AB 12 34 56 Q", "credentials" -> "xxxxxxx")
        )
      )

      whenReady(result) { _ =>
        status(result) mustBe INTERNAL_SERVER_ERROR
      }
    }
  }

}

object GooglePassControllerSpec {
  implicit val hc: HeaderCarrier = HeaderCarrier()
  private val passId             = UUID.randomUUID().toString

  private val fakeRequestWithAuth =
    FakeRequest("GET", "/").withHeaders("Content-Type" -> "application/json", "Authorization" -> "Bearer 123")

  private val mockGooglePassService = mock[GooglePassService]
  private val mockAuthConnector     = mock[AuthConnector]

  val retrievalResult: Future[Option[String] ~ Option[CredentialRole] ~ Option[String]] =
    Future.successful(new ~(new ~(Some("AB123456Q"), Some(User)), Some("id")))

  when(
    mockAuthConnector.authorise[Option[String] ~ Option[CredentialRole] ~ Option[String]](
      any[Predicate],
      any[Retrieval[Option[String] ~ Option[CredentialRole] ~ Option[String]]]
    )(any[HeaderCarrier], any[ExecutionContext])
  )
    .thenReturn(retrievalResult)

  val modules: Seq[GuiceableModule] =
    Seq(
      bind[GooglePassService].toInstance(mockGooglePassService),
      bind[AuthConnector].toInstance(mockAuthConnector)
    )

  val application: Application = new GuiceApplicationBuilder()
    .configure(conf = "auditing.enabled" -> false, "metrics.enabled" -> false, "metrics.jvm" -> false)
    .overrides(modules: _*)
    .build()
  private val controller       = application.injector.instanceOf[GooglePassController]

}
