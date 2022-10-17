/*
 * Copyright 2022 HM Revenue & Customs
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

import models.ApplePassDetails
import org.mockito.ArgumentMatchers.{any, anyString}
import org.mockito.ArgumentMatchersSugar.eqTo
import org.mockito.MockitoSugar
import org.mockito.MockitoSugar.mock
import org.scalatest.BeforeAndAfter
import org.scalatest.concurrent.ScalaFutures.whenReady
import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatest.wordspec.AnyWordSpec
import play.api.Application
import play.api.http.Status.OK
import play.api.inject.bind
import play.api.inject.guice.{GuiceApplicationBuilder, GuiceableModule}
import play.api.libs.json.{JsObject, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.ApplePassService
import uk.gov.hmrc.http.HeaderCarrier

import java.util.UUID
import scala.concurrent.Future

class ApplePassControllerSpec extends AnyWordSpec with Matchers with MockitoSugar with BeforeAndAfter {

  import ApplePassControllerSpec._

  "createPass" must {
    "should return OK with the uuid of the pass" in {
      when(mockApplePassService.createPass(eqTo("TestName TestSurname"), eqTo("AB 12 34 56 Q"), anyString())(any()))
        .thenReturn(Right(passId))

      val result = controller.createPass()(fakeRequest.withJsonBody(createPassRequest))

      whenReady(result) { _ =>
        status(result) mustBe OK
        contentAsString(result) mustBe passId
      }
    }
  }

  "getPassDetailsByPassId" must {
    "should return OK with the details of pass" in {
      when(mockApplePassService.getPassDetails(eqTo(passId))(any()))
        .thenReturn(Future.successful(Some(ApplePassDetails("TestName TestSurname", "AB 12 34 56 Q"))))

      val result = controller.getPassDetails(passId)(fakeRequest.withHeaders())

      whenReady(result) { _ =>
        status(result) mustBe OK
        contentAsJson(result).toString() mustBe createPassRequest.toString()
      }
    }

    "should return NotFound when there is no record for given passId" in {
      when(mockApplePassService.getPassDetails(eqTo(passId))(any()))
        .thenReturn(Future.successful(None))

      val result = controller.getPassDetails(passId)(fakeRequest.withHeaders())

      whenReady(result) { _ =>
        status(result) mustBe NOT_FOUND
      }
    }
  }

  "getPassCardByPassId" must {
    "should return OK with the byte data of pass" in {
      when(mockApplePassService.getPassCardByPassId(eqTo(passId))(any()))
        .thenReturn(Future.successful(Some("SomePassCodeData".getBytes())))

      val result = controller.getPassCardByPassId(passId)(fakeRequest.withHeaders())

      whenReady(result) { _ =>
        status(result) mustBe OK
        contentAsBytes(result).length should be > 1
      }
    }

    "should return NotFound when there is no record for given passId" in {
      when(mockApplePassService.getPassCardByPassId(eqTo(passId))(any()))
        .thenReturn(Future.successful(None))

      val result = controller.getPassCardByPassId(passId)(fakeRequest.withHeaders())

      whenReady(result) { _ =>
        status(result) mustBe NOT_FOUND
      }
    }
  }

  "getQrCodeByPassId" must {
    "should return OK with the byte data of qr code" in {
      when(mockApplePassService.getQrCodeByPassId(eqTo(passId))(any()))
        .thenReturn(Future.successful(Some("SomeQrCodeData".getBytes())))

      val result = controller.getQrCodeByPassId(passId)(fakeRequest.withHeaders())

      whenReady(result) { _ =>
        status(result) mustBe OK
        contentAsBytes(result).length should be > 1
      }
    }

    "should return NotFound when there is no record for given passId" in {
      when(mockApplePassService.getQrCodeByPassId(eqTo(passId))(any()))
        .thenReturn(Future.successful(None))

      val result = controller.getQrCodeByPassId(passId)(fakeRequest.withHeaders())

      whenReady(result) { _ =>
        status(result) mustBe NOT_FOUND
      }
    }
  }
}

object ApplePassControllerSpec {
  implicit val hc: HeaderCarrier = HeaderCarrier()
  private val passId = UUID.randomUUID().toString
  private val createPassRequest: JsObject = Json.obj("fullName" -> "TestName TestSurname", "nino" -> "AB 12 34 56 Q")

  private val fakeRequest = FakeRequest("GET", "/")
  private val mockApplePassService = mock[ApplePassService]

  val modules: Seq[GuiceableModule] =
    Seq(
      bind[ApplePassService].toInstance(mockApplePassService)
    )

  val application: Application = new GuiceApplicationBuilder()
    .configure(conf = "auditing.enabled" -> false, "metrics.enabled" -> false, "metrics.jvm" -> false).
    overrides(modules: _*).build()
  private val controller = application.injector.instanceOf[ApplePassController]
}

