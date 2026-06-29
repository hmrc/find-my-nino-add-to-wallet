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

package repositories.cache

import org.mongodb.scala.model.IndexModel
import play.api.libs.json.{Reads, Writes}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.mongo.cache.{CacheIdType, DataKey, MongoCacheRepository}
import uk.gov.hmrc.mongo.{MongoComponent, MongoDatabaseCollection, TimestampSupport}
import uk.gov.hmrc.mdc.Mdc

import javax.inject.{Inject, Singleton}
import scala.concurrent.duration.Duration
import scala.concurrent.{ExecutionContext, Future}

case object NinoCacheId extends CacheIdType[Nino] {
  override def run: Nino => String = _.nino
}

@Singleton
abstract class SessionCacheRepository @Inject() (
  mongoComponent: MongoComponent,
  override val collectionName: String,
  replaceIndexes: Boolean = true,
  ttl: Duration,
  timestampSupport: TimestampSupport
)(implicit ec: ExecutionContext)
    extends MongoDatabaseCollection {
  /*
    This class exists in hmrc-mongo library but uses the sessionId from the request session.
    This service is backend-only, so entries are keyed by NINO instead.
   */

  private val cacheRepo: MongoCacheRepository[Nino] = new MongoCacheRepository[Nino](
    mongoComponent = mongoComponent,
    collectionName = collectionName,
    replaceIndexes = replaceIndexes,
    ttl = ttl,
    timestampSupport = timestampSupport,
    cacheIdType = NinoCacheId
  )

  override val indexes: Seq[IndexModel] =
    cacheRepo.indexes

  def putSession[T: Writes](
    dataKey: DataKey[T],
    data: T,
    nino: Nino
  )(implicit ec: ExecutionContext): Future[(String, String)] =
    Mdc.preservingMdc {
      cacheRepo
        .put[T](nino)(dataKey, data)
        .map(res => "nino" -> res.id)
    }

  def getFromSession[T: Reads](dataKey: DataKey[T], nino: Nino): Future[Option[T]] =
    Mdc.preservingMdc {
      cacheRepo.get[T](nino)(dataKey)
    }

  def deleteFromSession[T](dataKey: DataKey[T], nino: Nino): Future[Unit] =
    Mdc.preservingMdc {
      cacheRepo.delete(nino)(dataKey)
    }

  def deleteAllFromSession(nino: Nino): Future[Unit] =
    Mdc.preservingMdc {
      cacheRepo.deleteEntity(nino)
    }
}
