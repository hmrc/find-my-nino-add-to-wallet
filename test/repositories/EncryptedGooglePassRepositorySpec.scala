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
import models.encryption.EncryptedGooglePass
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

import java.time.{ZoneId, ZonedDateTime}
import scala.concurrent.ExecutionContext.Implicits.global

class EncryptedGooglePassRepositorySpec
    extends AnyWordSpec
    with MockitoSugar
    with Matchers
    with DefaultPlayMongoRepositorySupport[EncryptedGooglePass]
    with ScalaFutures
    with IntegrationPatience
    with OptionValues { // scalastyle:off magic.number

  private val appConfig                = mock[AppConfig]
  when(appConfig.cacheTtl) thenReturn 1
  when(appConfig.encryptionKey) thenReturn "z4rWoRLf7a1OHTXLutSDJjhrUzZTBE3b"
  private val DEFAULT_EXPIRATION_YEARS = 100

  override protected val repository: EncryptedGooglePassRepository =
    new EncryptedGooglePassRepository(mongoComponent, appConfig)

  "insert" must {
    "save a new Google Pass in Mongo collection when collection is empty" in {

      val passId  = "test-pass-id-001"
      val record  = (
        passId,
        "Name Surname",
        "AB 12 34 56 Q",
        ZonedDateTime.now(ZoneId.of("UTC")).plusYears(DEFAULT_EXPIRATION_YEARS).toString,
        "http://test.com/test",
        Array[Byte](10)
      )
      val filters = Filters.eq("passId", passId)

      val documentsInDB = for {
        _             <- repository.insert(record._1, record._2, record._3, record._4, record._5, record._6)
        documentsInDB <- repository.collection.find[EncryptedGooglePass](filters).toFuture()
      } yield documentsInDB

      whenReady(documentsInDB, timeout = Timeout(Span(500L, Milliseconds))) { documentsInDB =>
        documentsInDB.size mustBe 1
      }
    }
  }

  "findByPassId" must {
    "retrieve existing Google Pass in Mongo collection" in {

      val passId = "test-pass-id-002"
      val record = (
        passId,
        "Name Surname",
        "AB 12 34 56 Q",
        ZonedDateTime.now(ZoneId.of("UTC")).plusYears(DEFAULT_EXPIRATION_YEARS).toString(),
        "http://test.com/test",
        Array[Byte](10)
      )

      val documentsInDB = for {
        _             <- repository.insert(record._1, record._2, record._3, record._4, record._5, record._6)
        documentsInDB <- repository.findByPassId(passId)
      } yield documentsInDB

      whenReady(documentsInDB, timeout = Timeout(Span(500L, Milliseconds))) { documentsInDB =>
        documentsInDB.isDefined mustBe true
      }
    }
  }
}
