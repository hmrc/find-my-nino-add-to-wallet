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

import com.github.simplyscala.MongoEmbedDatabase
import org.mockito.MockitoSugar
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.mongo.MongoComponent

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class GooglePassRepositorySpec extends AnyWordSpec with MockitoSugar with Matchers with MongoEmbedDatabase
  with BeforeAndAfterAll { // scalastyle:off magic.number
/*
  import GooglePassRepositorySpec._

  override def beforeAll(): Unit = {
    super.beforeAll()
    mongoStart(port = databasePort)
  }

  "insert" must {
    "save a new Google Pass in Mongo collection when collection is empty" in {
      mongoCollectionDrop()

      val passId = "test-pass-id-001"
      val record = (passId,
        "Name Surname",
        "AB 12 34 56 Q",
        DateTime.now(DateTimeZone.UTC).plusYears(DEFAULT_EXPIRATION_YEARS).toString(),
        Array[Byte](10),
        Array[Byte](10)
      )
      val filters = Filters.eq("passId", passId)

      val documentsInDB = for {
        _ <- googlePassRepository.insert(record._1, record._2, record._3, record._4, record._5, record._6)
        documentsInDB <- googlePassRepository.collection.find[GooglePass](filters).toFuture()
      } yield documentsInDB

      whenReady(documentsInDB, timeout = Timeout(Span(500L, Milliseconds))) { documentsInDB =>
        documentsInDB.size mustBe 1
      }
    }
  }

  "findByPassId" must {
    "retrieve existing Google Pass in Mongo collection" in {
      mongoCollectionDrop()

      val passId = "test-pass-id-002"
      val record = (passId, "Name Surname", "AB 12 34 56 Q", DateTime.now(DateTimeZone.UTC).plusYears(DEFAULT_EXPIRATION_YEARS).toString(), Array[Byte](10), Array[Byte](10))

      val documentsInDB = for {
        _ <- googlePassRepository.insert(record._1, record._2, record._3, record._4, record._5, record._6)
        documentsInDB <- googlePassRepository.findByPassId(passId)
      } yield documentsInDB

      whenReady(documentsInDB, timeout = Timeout(Span(500L, Milliseconds))) { documentsInDB =>
        documentsInDB.isDefined mustBe true
      }
    }
  }*/
}

object GooglePassRepositorySpec extends AnyWordSpec with MockitoSugar {

  import scala.concurrent.ExecutionContext.Implicits._

  private val databaseName = "find-my-nino-add-to-wallet"
  private val databasePort = 12345
  private val mongoUri = s"mongodb://127.0.0.1:$databasePort/$databaseName?heartbeatFrequencyMS=1000&rm.failover=default"
  private val mongoComponent = MongoComponent(mongoUri)
  private val DEFAULT_EXPIRATION_YEARS = 100

  private def mongoCollectionDrop(): Void =
    Await.result(googlePassRepository.collection.drop().toFuture(), Duration.Inf)

  def googlePassRepository: GooglePassRepository = new GooglePassRepository(mongoComponent)
}
