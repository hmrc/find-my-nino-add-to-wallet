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

package repositories

import com.google.inject.{Inject, Singleton}
import config.AppConfig
import models.apple.ApplePass
import org.mongodb.scala.model.{Filters, IndexModel, IndexOptions, Indexes}
import play.api.Logging
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import java.util.concurrent.TimeUnit
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

@Singleton
class ApplePassRepository @Inject()(
                                     mongoComponent: MongoComponent,
                                     appConfig: AppConfig
                                   )(implicit ec: ExecutionContext) extends PlayMongoRepository[ApplePass](
  collectionName = "apple-pass",
  mongoComponent = mongoComponent,
  domainFormat = ApplePass.mongoFormat,
  indexes = Seq(
    IndexModel(
      Indexes.ascending("passId"),
      IndexOptions().name("passId").unique(true)
    ),
    IndexModel(
      Indexes.ascending("fullName", "nino"),
      IndexOptions().name("fullName_Nino")
    ),
    IndexModel(
      Indexes.ascending("lastUpdated"),
      IndexOptions()
        .name("lastUpdatedIdx")
        .expireAfter(appConfig.cacheTtl, TimeUnit.SECONDS)
    )
  ),
  replaceIndexes = true
) with Logging with ApplePassRepoTrait {

  def insert(passId: String,
             fullName: String,
             nino: String,
             applePassCard: Array[Byte],
             qrCode: Array[Byte])
            (implicit ec: ExecutionContext): Future[Unit] = {
    logger.info(s"Inserted one in $collectionName table")
    collection.insertOne(ApplePass(passId, fullName, nino, applePassCard, qrCode))
      .head()
      .map(_ => ())
      .recoverWith {
        case e => Future.successful(logger.info(s"failed to insert apple pass card into $collectionName table with ${e.getMessage}"))
      }
  }

  def findByPassId(passId: String)(implicit ec: ExecutionContext): Future[Option[ApplePass]] =
    collection.find(Filters.equal("passId", passId))
      .headOption()

}