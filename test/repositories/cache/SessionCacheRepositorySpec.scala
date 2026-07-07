/*
 * Copyright 2026 HM Revenue & Customs
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

package repositories.cache

import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.concurrent.IntegrationPatience
import org.scalatest.time.{Milliseconds, Span}
import uk.gov.hmrc.domain.{Generator, Nino}
import uk.gov.hmrc.mongo.cache.DataKey
import util.SpecBase

import java.util.UUID
import scala.util.Random

class SessionCacheRepositorySpec extends SpecBase with IntegrationPatience {

  private lazy val repository: FMNSessionCacheRepository = inject[FMNSessionCacheRepository]

  private def randomNino(): Nino = Nino(new Generator(new Random()).nextNino.nino)

  private def randomDataKey(prefix: String): DataKey[String] = DataKey[String](s"$prefix-${UUID.randomUUID()}")

  "putSession and getFromSession" must {
    "return the cached value for the same nino and data key" in {
      val nino    = randomNino()
      val dataKey = randomDataKey("individual-details")
      val value   = s"value-${UUID.randomUUID()}"

      val result = for {
        _      <- repository.putSession(dataKey, value, nino)
        cached <- repository.getFromSession(dataKey, nino)
      } yield cached

      whenReady(result, timeout = Timeout(Span(500L, Milliseconds))) { cached =>
        cached mustBe Some(value)
      }
    }

    "return none when no value has been cached for the nino and data key" in {
      val nino    = randomNino()
      val dataKey = randomDataKey("individual-details")

      whenReady(repository.getFromSession(dataKey, nino), timeout = Timeout(Span(500L, Milliseconds))) { cached =>
        cached mustBe None
      }
    }

    "keep cached values isolated between different ninos for the same data key" in {
      val firstNino  = randomNino()
      val secondNino = randomNino()
      val dataKey    = randomDataKey("individual-details")

      val result = for {
        _            <- repository.putSession(dataKey, "first-value", firstNino)
        firstCached  <- repository.getFromSession(dataKey, firstNino)
        secondCached <- repository.getFromSession(dataKey, secondNino)
      } yield (firstCached, secondCached)

      whenReady(result, timeout = Timeout(Span(500L, Milliseconds))) { case (firstCached, secondCached) =>
        firstCached mustBe Some("first-value")
        secondCached mustBe None
      }
    }
  }

  "deleteFromSession" must {
    "remove only the cached value for the matching nino" in {
      val firstNino  = randomNino()
      val secondNino = randomNino()
      val dataKey    = randomDataKey("individual-details")

      val result = for {
        _                       <- repository.putSession(dataKey, "first-value", firstNino)
        _                       <- repository.putSession(dataKey, "second-value", secondNino)
        _                       <- repository.deleteFromSession(dataKey, firstNino)
        firstCachedAfterDelete  <- repository.getFromSession(dataKey, firstNino)
        secondCachedAfterDelete <- repository.getFromSession(dataKey, secondNino)
      } yield (firstCachedAfterDelete, secondCachedAfterDelete)

      whenReady(result, timeout = Timeout(Span(500L, Milliseconds))) {
        case (firstCachedAfterDelete, secondCachedAfterDelete) =>
          firstCachedAfterDelete mustBe None
          secondCachedAfterDelete mustBe Some("second-value")
      }
    }
  }

  "deleteAllFromSession" must {
    "remove all cached values for one nino without affecting other ninos" in {
      val firstNino  = randomNino()
      val secondNino = randomNino()
      val firstKey   = randomDataKey("first-key")
      val secondKey  = randomDataKey("second-key")
      val sharedKey  = randomDataKey("shared-key")

      val result = for {
        _               <- repository.putSession(firstKey, "first-value", firstNino)
        _               <- repository.putSession(secondKey, "second-value", firstNino)
        _               <- repository.putSession(sharedKey, "other-nino-value", secondNino)
        _               <- repository.deleteAllFromSession(firstNino)
        firstCached     <- repository.getFromSession(firstKey, firstNino)
        secondCached    <- repository.getFromSession(secondKey, firstNino)
        otherNinoCached <- repository.getFromSession(sharedKey, secondNino)
      } yield (firstCached, secondCached, otherNinoCached)

      whenReady(result, timeout = Timeout(Span(500L, Milliseconds))) {
        case (firstCached, secondCached, otherNinoCached) =>
          firstCached mustBe None
          secondCached mustBe None
          otherNinoCached mustBe Some("other-nino-value")
      }
    }
  }
}
