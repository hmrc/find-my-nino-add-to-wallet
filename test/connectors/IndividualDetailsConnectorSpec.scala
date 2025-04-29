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

import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, get, urlEqualTo}
import config.AppConfig
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.*
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.{Application, inject}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers.*
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.HeaderCarrier
import util.WireMockHelper

import scala.concurrent.ExecutionContext

class IndividualDetailsConnectorSpec
    extends PlaySpec
    with GuiceOneAppPerSuite
    with WireMockHelper
    with MockitoSugar
    with ScalaFutures {

  override def fakeApplication(): Application = {
    server.start()
    new GuiceApplicationBuilder()
      .configure(
        "external-url.individual-details.port"          -> server.port(),
        "external-url.individual-details.host"          -> "127.0.0.1",
        "external-url.individual-details.protocol"      -> "http",
        "external-url.individual-details.auth-token"    -> "token1",
        "external-url.individual-details.environment"   -> "env1",
        "external-url.individual-details.originator-id" -> "id1"
      )
      .build()
  }

  implicit val hc: HeaderCarrier    = HeaderCarrier()
  implicit val ec: ExecutionContext = app.injector.instanceOf[ExecutionContext]

  lazy val connector: IndividualDetailsConnector = {
    val httpClientV2 = app.injector.instanceOf[HttpClientV2]
    val appConfig    = app.injector.instanceOf[AppConfig]

    new IndividualDetailsConnector(httpClientV2, appConfig)
  }

  "IndividualDetailsConnector" should {

    "return the expected result from getIndividualDetails" in {
      val nino: String         = "AB123456C"
      val resolveMerge: String = "Y"
      val urlPath              = s"/individuals/details/NINO/${nino.take(8)}?resolveMerge=$resolveMerge"

      server.stubFor(
        get(urlEqualTo(urlPath))
          .willReturn(aResponse().withStatus(OK).withBody("response body"))
      )

      val result = connector.getIndividualDetails(nino, resolveMerge).futureValue

      result.status mustBe OK
      result.body mustBe "response body"
    }
  }
}
