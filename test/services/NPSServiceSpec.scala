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

package services

import connectors.NPSConnector
import models.nps.ChildReferenceNumberUpliftRequest
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import play.api.Application
import play.api.http.Status.NO_CONTENT
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import play.api.inject.bind
import util.SpecBase

import scala.concurrent.Future

class NPSServiceSpec extends SpecBase {

  private val mockNPSConnector = mock[NPSConnector]

  override implicit lazy val app: Application = localGuiceApplicationBuilder()
    .overrides(
      bind[NPSConnector].toInstance(mockNPSConnector)
    )
    .build()

  val npsService: NPSService = app.injector.instanceOf[NPSService]

  override def beforeEach(): Unit =
    reset(mockNPSConnector)

  "upliftCRN" must {
    "return 204 response when CRN is uplifted successfully" in {

      implicit val hc: HeaderCarrier = HeaderCarrier()
      val npsRequest                 = ChildReferenceNumberUpliftRequest("test", "test", "01/01/1990")
      val nino                       = "AA000003B"

      when(
        mockNPSConnector
          .upliftCRN(any, any)(any, any())
      )
        .thenReturn(Future.successful(HttpResponse(NO_CONTENT, "")))

      val result = npsService.upliftCRN(nino, npsRequest)

      whenReady(result) {
        _.status mustBe NO_CONTENT
      }
    }
  }
}
