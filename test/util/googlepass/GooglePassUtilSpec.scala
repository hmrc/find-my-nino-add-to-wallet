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

package util.googlepass

import com.google.auth.oauth2.GoogleCredentials
import config.AppConfig
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import services.googlepass.{CreateGenericPrivatePass, GooglePassUtil}
class GooglePassUtilSpec extends AsyncWordSpec with Matchers with MockitoSugar {

  val mockConfig: AppConfig                                  = mock[AppConfig]
  val mockCreateGenericPrivatePass: CreateGenericPrivatePass = mock[CreateGenericPrivatePass]
  val mockGoogleCredentials: GoogleCredentials               = mock[GoogleCredentials]

  val googlePassUtil: GooglePassUtil = new GooglePassUtil(mockConfig, mockCreateGenericPrivatePass)

  when(mockCreateGenericPrivatePass.createJwtWithCredentials(any, any, any, any, any)) thenReturn "testJwt"

  "GooglePassUtil createGooglePass" must {
    "must return valid url" in {
      val result = googlePassUtil.createGooglePassWithCredentials("test name", "AB 01 23 45 C", mockGoogleCredentials)

      "https://pay.google.com/gp/v/save/" + result mustBe "https://pay.google.com/gp/v/save/testJwt"
    }
  }
}
