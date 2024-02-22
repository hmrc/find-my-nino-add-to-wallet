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
import models.google.GooglePass
import org.mongodb.scala.model.{Filters, IndexModel, IndexOptions, Indexes}
import play.api.Logging
import repositories.encryption.EncryptedGooglePass
import repositories.encryption.EncryptedGooglePass._
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import java.util.concurrent.TimeUnit
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

@Singleton
class EncryptedGooglePassRepository @Inject()(
                                     mongoComponent: MongoComponent,
                                     appConfig: AppConfig
                                   )(implicit ec: ExecutionContext) extends PlayMongoRepository[EncryptedGooglePass](
  collectionName = "google-pass",
  mongoComponent = mongoComponent,
  domainFormat = EncryptedGooglePass.encryptedFormat,
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
) with Logging {
  def insert(passId: String,
             fullName: String,
             nino: String,
             expirationDate: String,
             googlePassUrl: String,
             qrCode: Array[Byte])
            (implicit ec: ExecutionContext): Future[Unit] = {
    logger.info(s"Inserted one in $collectionName table")
    collection.insertOne(encrypt(GooglePass(passId, fullName, nino, expirationDate, googlePassUrl, qrCode), appConfig.encryptionKey))
      .head()
      .map(_ => ())
      .recoverWith {
        case e => Future.successful(logger.info(s"failed to insert google pass card into $collectionName table with ${e.getMessage}"))
      }
  }

  def findByPassId(passId: String)(implicit ec: ExecutionContext): Future[Option[GooglePass]] =
    collection.find(Filters.equal("passId", passId))
      .first()
      .toFutureOption()
      .map(optEncryptedGooglePass =>
        optEncryptedGooglePass.map(encryptedGooglePass => decrypt(encryptedGooglePass, appConfig.encryptionKey))
      )
}