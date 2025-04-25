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

import config.AppConfig
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.*
import play.api.test.Helpers.*
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder}
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, HttpResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class IndividualDetailsConnectorSpec extends PlaySpec with MockitoSugar {

  "IndividualDetailsConnector" should {

    "return the expected result from getIndividualDetails" in {
      val mockHttpClientV2   = mock[HttpClientV2]
      val mockConfig         = mock[AppConfig]
      val mockRequestBuilder = mock[RequestBuilder]
      val connector          = new IndividualDetailsConnector(mockHttpClientV2, mockConfig)
      val nino               = "AB123456C"
      val resolveMerge       = "Y"
      val expectedResponse   = HttpResponse(OK, "response body")

      implicit val hc: HeaderCarrier = HeaderCarrier()

      when(mockConfig.individualDetailsToken).thenReturn("token")
      when(mockConfig.individualDetailsEnvironment).thenReturn("environment")
      when(mockConfig.individualDetailsOriginatorId).thenReturn("originatorId")
      when(mockRequestBuilder.execute(any[HttpReads[HttpResponse]], any[ExecutionContext]))
        .thenReturn(Future.successful(expectedResponse))

      when(mockHttpClientV2.get(any())(any[HeaderCarrier])).thenReturn(mockRequestBuilder)

      val result = await(connector.getIndividualDetails(nino, resolveMerge))

      result mustBe expectedResponse
    }
  }
}
