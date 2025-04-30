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

package util

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.http.Fault
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.http.{HeaderNames, MimeTypes, Status}
import play.api.inject.guice.{GuiceApplicationBuilder, GuiceableModule}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext

trait WiremockStub
    extends AnyWordSpec
    with GuiceOneAppPerSuite
    with Status
    with HeaderNames
    with MimeTypes
    with Matchers
    with ScalaFutures
    with IntegrationPatience {

  implicit val hc: HeaderCarrier    = HeaderCarrier()
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

  val server: WireMockServer

  implicit def app(confStrings: Map[String, Any], overrides: GuiceableModule*): Application =
    new GuiceApplicationBuilder()
      .configure(confStrings)
      .overrides(overrides: _*)
      .build()

  def stubGet(url: String, responseStatus: Int, responseBody: Option[String]): StubMapping = server.stubFor {
    val baseResponse = aResponse().withStatus(responseStatus).withHeader(CONTENT_TYPE, JSON)
    val response     = responseBody.fold(baseResponse)(body => baseResponse.withBody(body))

    get(url).willReturn(response)
  }

  def stubPut(
    url: String,
    responseStatus: Int,
    requestBody: Option[String],
    responseBody: Option[String]
  ): StubMapping = server.stubFor {
    val baseResponse = aResponse().withStatus(responseStatus).withHeader(CONTENT_TYPE, JSON)
    val response     = responseBody.fold(baseResponse)(body => baseResponse.withBody(body))

    requestBody.fold(put(url).willReturn(response))(requestBody =>
      put(url).withRequestBody(equalToJson(requestBody)).willReturn(response)
    )
  }

  def stubPost(
    url: String,
    responseStatus: Int,
    requestBody: Option[String],
    responseBody: Option[String]
  ): StubMapping = server.stubFor {
    val baseResponse = aResponse().withStatus(responseStatus).withHeader(CONTENT_TYPE, JSON)
    val response     = responseBody.fold(baseResponse)(body => baseResponse.withBody(body))

    requestBody.fold(post(url).willReturn(response))(requestBody =>
      post(url).withRequestBody(equalToJson(requestBody)).willReturn(response)
    )
  }

  def stubWithFault(url: String, requestBody: Option[String], fault: Fault): StubMapping = server.stubFor {
    val response = aResponse().withFault(fault)

    requestBody.fold(any(urlEqualTo(url)).willReturn(response))(requestBody =>
      any(urlEqualTo(url)).withRequestBody(equalToJson(requestBody)).willReturn(response)
    )
  }

  def stubWithDelay(
    url: String,
    responseStatus: Int,
    requestBody: Option[String],
    responseBody: Option[String],
    delay: Int
  ): StubMapping = server.stubFor {
    val baseResponse = aResponse().withStatus(responseStatus).withHeader(CONTENT_TYPE, JSON).withFixedDelay(delay)
    val response     = responseBody.fold(baseResponse)(body => baseResponse.withBody(body))

    requestBody.fold(any(urlEqualTo(url)).willReturn(response))(requestBody =>
      any(urlEqualTo(url)).withRequestBody(equalToJson(requestBody)).willReturn(response)
    )
  }

  def verifyCorrelationIdHeader(requestPattern: RequestPatternBuilder): Unit =
    server.verify(
      requestPattern.withHeader(
        "Correlation-Id",
        matching("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}")
      )
    )

  def stubNPS(nino: String, response: Int) =
    server.stubFor(
      post(urlMatching(s"/nps/nps-json-service/nps/itmp/find-my-nino/api/v1/individual/$nino"))
        .willReturn(
          aResponse()
            .withStatus(response)
            .withBody(
              s"""{
               |}""".stripMargin
            )
        )
    )
}
