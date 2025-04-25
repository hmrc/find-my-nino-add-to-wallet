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

package repositories

import config.AppConfig
import models.apple.ApplePass
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import org.mongodb.scala.model.Filters
import org.mongodb.scala.ObservableFuture
import org.scalatest.OptionValues
import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.must.Matchers
import org.scalatest.time.{Milliseconds, Span}
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport

import scala.concurrent.ExecutionContext.Implicits.global

class ApplePassRepositorySpec
    extends AnyWordSpec
    with MockitoSugar
    with Matchers
    with DefaultPlayMongoRepositorySupport[ApplePass]
    with ScalaFutures
    with IntegrationPatience
    with OptionValues { // scalastyle:off magic.number

  private val mockAppConfig = mock[AppConfig]

  when(mockAppConfig.cacheTtl) thenReturn 1
  when(mockAppConfig.encryptionKey) thenReturn "z4rWoRLf7a1OHTXLutSDJjhrUzZTBE3b"

  override protected val repository: ApplePassRepository = new ApplePassRepository(mongoComponent, mockAppConfig)

  "insert" must {
    "save a new Apple Pass in Mongo collection when collection is empty" in {

      val passId  = "test-pass-id-001"
      val record  = (passId, "Name Surname", "AB 12 34 56 Q", Array[Byte](10), Array[Byte](10))
      val filters = Filters.eq("passId", passId)

      val documentsInDB = for {
        _             <- repository.insert(record._1, record._2, record._3, record._4, record._5)
        documentsInDB <- repository.collection.find[ApplePass](filters).toFuture()
      } yield documentsInDB

      whenReady(documentsInDB, timeout = Timeout(Span(500L, Milliseconds))) { documentsInDB =>
        documentsInDB.size mustBe 1
      }
    }
  }

  "findByPassId" must {
    "retrieve existing Apple Pass in Mongo collection" in {

      val passId = "test-pass-id-002"
      val record = (passId, "Name Surname", "AB 12 34 56 Q", Array[Byte](10), Array[Byte](10))

      val documentsInDB = for {
        _             <- repository.insert(record._1, record._2, record._3, record._4, record._5)
        documentsInDB <- repository.findByPassId(passId)
      } yield documentsInDB

      whenReady(documentsInDB, timeout = Timeout(Span(500L, Milliseconds))) { documentsInDB =>
        documentsInDB.isDefined mustBe true
      }
    }
  }
}
