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

package services

import config.AppConfig
import connectors.IndividualDetailsConnector
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.*
import play.api.test.Helpers.*
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class IndividualDetailsServiceSpec extends PlaySpec with MockitoSugar {

  "IndividualDetailsService" should {

    "return the expected result from getIndividualDetails" in {
      val mockConnector = mock[IndividualDetailsConnector]
      val service       = new IndividualDetailsService(mockConnector)

      val nino         = "AB123456C"
      val resolveMerge = "true"

      val expectedResponse = HttpResponse(OK, "response body")
      when(
        mockConnector.getIndividualDetails(
          org.mockito.ArgumentMatchers.eq(nino),
          org.mockito.ArgumentMatchers.eq(resolveMerge)
        )(any[ExecutionContext], any[HeaderCarrier])
      )
        .thenReturn(Future.successful(expectedResponse))

      implicit val hc: HeaderCarrier = HeaderCarrier()
      val result                     = await(service.getIndividualDetails(nino, resolveMerge))

      result mustBe expectedResponse
    }
  }
}
