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

import config.AppConfig
import org.mockito.ArgumentMatchers.{any, anyString, eq => eqTo}
import org.mockito.MockitoSugar
import org.mockito.MockitoSugar.mock
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import repositories.{PersonDetailsRepo, RowPersonDetails}

import scala.concurrent.Future

class PersonDetailsServiceSpec extends AsyncWordSpec with Matchers with MockitoSugar with BeforeAndAfterEach {

  import PersonDetailsServiceSpec._

  override def beforeEach(): Unit = {
      reset(mockPersonDetailsRepository, mockAppConfig)
  }

  "getPersonDetailsById" must {
    "return the details when pdid exists" in {
      val pdId = "test1"
      val personDetails = RowPersonDetails(
        pdId,
        "Test Name",
        "AB 12 34 56 Q",
        "Test Details",
        "test date created"
      )
      when(mockPersonDetailsRepository.findById(eqTo(pdId))(any()))
        .thenReturn(Future.successful(Option(personDetails)))

      personDetailsService.getPersonDetailsById(pdId)(implicitly).map { result =>
        result mustBe Some("Test Details")
      }
    }

    "return None when pdid does NOT exist" in {
      when(mockPersonDetailsRepository.findById(eqTo("test2"))(any()))
        .thenReturn(Future.successful(None))

      personDetailsService.getPersonDetailsById("test2")(implicitly).map { result =>
        result mustBe None
      }
    }
  }

  "createPersonDetails" must {
    "return an uuid when successfully inserted" in {
      val eitherResult = personDetailsService.createPersonDetails(
        "TestName TestSurname",
        "AB 12 34 56 Q",
        "test details",
        "test date")

      eitherResult.isLeft mustBe false
      eitherResult match {
        case Right(uuid) =>
          verify(mockPersonDetailsRepository, times(1)).insert(anyString(), eqTo("TestName TestSurname"), eqTo("AB 12 34 56 Q"), any(), any())(any())
          uuid.length mustBe 36
      }
    }
  }
}

object PersonDetailsServiceSpec {

  private val mockPersonDetailsRepository = mock[PersonDetailsRepo]
  private val mockAppConfig = mock[AppConfig]

  val personDetailsService = new PersonDetailsService(mockAppConfig, mockPersonDetailsRepository)
}
