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
import org.joda.time.{DateTime, DateTimeZone}
import org.mongodb.scala.model.{Filters, IndexModel, IndexOptions, Indexes}
import play.api.Logging
import play.api.libs.json.{Format, Json}
import repositories.encryption.EncryptedRowPersonDetails
import repositories.encryption.EncryptedRowPersonDetails.{decrypt, encrypt}
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import uk.gov.hmrc.mongo.play.json.formats.{MongoBinaryFormats, MongoJodaFormats}

import scala.concurrent.{ExecutionContext, Future}

case class RowPersonDetails(detailsId: String,
                            fullName: String,
                            nino: String,
                            personDetails: String,
                            dateCreated: String,
                            lastUpdated: String)


object RowPersonDetails {

  def apply(detailsId: String,
            fullName: String,
            nino: String,
            personDetails: String,
            dateCreated: String): RowPersonDetails = {
    RowPersonDetails(detailsId, fullName, nino, personDetails, dateCreated, DateTime.now(DateTimeZone.UTC).toLocalDateTime.toString())
  }

  implicit val dateFormat: Format[DateTime] = MongoJodaFormats.dateTimeFormat
  implicit val arrayFormat: Format[Array[Byte]] = MongoBinaryFormats.byteArrayFormat
  implicit val mongoFormat: Format[RowPersonDetails] = Json.format[RowPersonDetails]
}

@Singleton
class PersonDetailsRepo @Inject()(mongoComponent: MongoComponent,
                                  appConfig: AppConfig)
                                 (implicit ec: ExecutionContext)
  extends PlayMongoRepository[EncryptedRowPersonDetails](
    collectionName = "person-details",
    mongoComponent = mongoComponent,
    domainFormat = EncryptedRowPersonDetails.encryptedFormat,
    indexes = Seq(
      IndexModel(
        Indexes.ascending("detailsId"),
        IndexOptions().name("detailsId").unique(true)
      ),
      IndexModel(
        Indexes.ascending("fullName", "nino"),
        IndexOptions().name("fullName_Nino")
      )
    )
  ) with Logging {
  def insert(detailsId: String,
             fullName: String,
             nino: String,
             personDetails: String,
             dateCreated: String)
            (implicit ec: ExecutionContext): Future[Unit] = {
    logger.info(s"Inserted one in $collectionName table")
    collection.insertOne(encrypt(RowPersonDetails(detailsId, fullName, nino, personDetails, dateCreated), appConfig.encryptionKey))
      .head()
      .map(_ => ())
      .recoverWith {
        case e => Future.successful(logger.info(s"failed to insert person details row into $collectionName table with ${e.getMessage}"))
      }

  }

  def findById(pdId: String)(implicit ec: ExecutionContext): Future[Option[RowPersonDetails]] =
    collection.find(Filters.equal("detailsId", pdId))
      .first()
      .toFutureOption()
      .map(optEncryptedRowPersonDetails =>
        optEncryptedRowPersonDetails.map(encryptedRowPersonDetails  => decrypt(encryptedRowPersonDetails , appConfig.encryptionKey))
      )
}

