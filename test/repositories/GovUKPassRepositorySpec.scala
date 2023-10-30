/*
 * Copyright 2023 HM Revenue & Customs
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

package repositories

import config.AppConfig
import org.mockito.MockitoSugar
import org.scalatest.BeforeAndAfter
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.mongo.MongoComponent

import scala.concurrent.ExecutionContext.Implicits.global

class GovUKPassRepositorySpec extends AnyWordSpec with Matchers with MockitoSugar with BeforeAndAfter {

  val mongoComponent = mock[MongoComponent]
  val appConfig = mock[AppConfig]

  "GovUKPassRepository" must {
    "be able to create a new instance" ignore {
      val repository = new GovUKPassRepository(mongoComponent, appConfig)
      val result = repository.insert("passId", List("TestGivenName1", "TestGivenName2"), "TestSurname", "AB 12 34 56 Q", "vcDocument", Array[Byte]())

      repository mustBe a[GovUKPassRepository]
      result mustBe a[scala.concurrent.Future[org.mongodb.scala.result.InsertOneResult]]
    }

  }
}