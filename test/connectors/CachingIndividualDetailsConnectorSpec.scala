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

package connectors

import cats.data.EitherT
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.Futures.PatienceConfig
import org.scalatest.time.{Seconds, Span}
import play.api.Application
import play.api.inject.bind
import play.api.libs.json.{Format, JsValue, Json}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import repositories.cache.FMNSessionCacheRepository
import services.SensitiveFormatService
import uk.gov.hmrc.domain.{Generator, Nino}
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}
import util.{SpecBase, WireMockHelper}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

class CachingIndividualDetailsConnectorSpec extends SpecBase with WireMockHelper with BeforeAndAfterEach {
  implicit override val patienceConfig: PatienceConfig = PatienceConfig(timeout = Span(20, Seconds))

  val mockUnderlying: DefaultIndividualDetailsConnector = mock[DefaultIndividualDetailsConnector]
  val mockCacheRepo: FMNSessionCacheRepository          = mock[FMNSessionCacheRepository]
  val mockFormatService: SensitiveFormatService         = mock[SensitiveFormatService]

  override implicit val hc: HeaderCarrier                       = HeaderCarrier()
  override lazy implicit val ec: ExecutionContext               = scala.concurrent.ExecutionContext.global
  implicit val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

  override implicit lazy val app: Application = localGuiceApplicationBuilder()
    .overrides(
      bind[DefaultIndividualDetailsConnector].toInstance(mockUnderlying),
      bind[FMNSessionCacheRepository].toInstance(mockCacheRepo),
      bind[SensitiveFormatService].toInstance(mockFormatService)
    )
    .build()

  private val nino                = Nino(new Generator(new Random()).nextNino.nino).nino
  private val resolveMerge        = ""
  private val jsonResult: JsValue = Json.obj("person" -> "test")

  private def connector: CachingIndividualDetailsConnector = app.injector.instanceOf[CachingIndividualDetailsConnector]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockUnderlying, mockCacheRepo, mockFormatService)

    when(mockFormatService.sensitiveFormatFromReadsWrites[JsValue])
      .thenReturn(Format.of[JsValue])
    ()
  }

  "CachingIndividualDetailsConnector.personDetails" should {

    "return cached value if present in session cache" in {
      when(mockCacheRepo.getFromSession[JsValue](any())(any(), any()))
        .thenReturn(Future.successful(Some(jsonResult)))

      val result = connector.getIndividualDetails(nino, resolveMerge).value.futureValue

      result mustBe Right(jsonResult)
      verify(mockUnderlying, times(0)).getIndividualDetails(any(), any())(any(), any())
    }

    "fetch person details and cache result if not present in session cache" in {
      when(mockCacheRepo.getFromSession[JsValue](any())(any(), any()))
        .thenReturn(Future.successful(None))

      when(mockUnderlying.getIndividualDetails(eqTo(nino), eqTo(resolveMerge))(any(), any()))
        .thenReturn(EitherT.rightT[Future, UpstreamErrorResponse](jsonResult))

      when(
        mockCacheRepo.putSession(any(), any())(any(), any(), any())
      ).thenReturn(Future.successful("sessionId" -> "updated"))

      val result = connector.getIndividualDetails(nino, resolveMerge).value.futureValue

      result mustBe Right(jsonResult)
      verify(mockUnderlying, times(1)).getIndividualDetails(eqTo(nino), eqTo(resolveMerge))(any(), any())
      verify(mockCacheRepo).putSession(
        any(),
        any()
      )(
        any[Format[JsValue]],
        any[HeaderCarrier],
        any[ExecutionContext]
      )
    }

    "return error if connector fails and nothing in cache" in {
      val error = UpstreamErrorResponse("Something went wrong", 500)

      when(mockCacheRepo.getFromSession[JsValue](any())(any(), any()))
        .thenReturn(Future.successful(None))

      when(mockUnderlying.getIndividualDetails(eqTo(nino), eqTo(resolveMerge))(any(), any()))
        .thenReturn(EitherT.leftT[Future, JsValue](error))
      val result = connector.getIndividualDetails(nino, resolveMerge).value.futureValue

      result mustBe Left(error)
    }
  }

  "deleteIndividualDetailsIfCached" should {

    "return unit when successful" in {
      when(mockCacheRepo.deleteFromSession[JsValue](any())(any()))
        .thenReturn(Future.successful((): Unit))

      val result = connector.deleteIndividualDetailsIfCached(nino).value.futureValue

      result mustBe Right((): Unit)
    }
  }

}
