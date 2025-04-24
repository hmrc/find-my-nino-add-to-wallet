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

package connectors

import config.AppConfig
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar
import org.scalatestplus.play._
import play.api.test.Helpers._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class IndividualDetailsConnectorSpec extends PlaySpec with MockitoSugar {

  "IndividualDetailsConnector" should {

    "return the expected result from getIndividualDetails" in {
      val mockHttpClient   = mock[HttpClient]
      val mockConfig       = mock[AppConfig]
      val connector        = new IndividualDetailsConnector(mockHttpClient, mockConfig)
      val nino             = "AB123456C"
      val resolveMerge     = "Y"
      val expectedResponse = HttpResponse(OK, "response body")

      implicit val hc: HeaderCarrier = HeaderCarrier()

      when(mockHttpClient.GET[HttpResponse](any, any(), any())(any(), any(), any()))
        .thenReturn(Future.successful(expectedResponse))

      val result = await(connector.getIndividualDetails(nino, resolveMerge, hc))

      result mustBe expectedResponse
    }
  }
}
