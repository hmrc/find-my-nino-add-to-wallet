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

import com.google.inject.{Inject, Singleton}
import org.joda.time.{DateTime, DateTimeZone}
import org.mongodb.scala.model.{Filters, IndexModel, IndexOptions, Indexes}
import play.api.Logging
import play.api.libs.json.{Format, Json}
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import uk.gov.hmrc.mongo.play.json.formats.{MongoBinaryFormats, MongoJodaFormats}

import scala.concurrent.{ExecutionContext, Future}

case class ApplePass(passId: String,
                     fullName: String,
                     nino: String,
                     expirationDate: String,
                     applePassCard: Array[Byte],
                     qrCode: Array[Byte],
                     lastUpdated: DateTime)

object ApplePass {
  def apply(passId: String, fullName: String, nino: String, expirationDate: String, applePassCard: Array[Byte], qrCode: Array[Byte]): ApplePass = {
    ApplePass(passId, fullName, nino, expirationDate: String, applePassCard, qrCode, DateTime.now(DateTimeZone.UTC))
  }

  implicit val dateFormat: Format[DateTime] = MongoJodaFormats.dateTimeFormat
  implicit val arrayFormat: Format[Array[Byte]] = MongoBinaryFormats.byteArrayFormat
  implicit val mongoFormat: Format[ApplePass] = Json.format[ApplePass]
}

@Singleton
class ApplePassRepository @Inject()(
                                     mongoComponent: MongoComponent
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
    )
  )
) with Logging {
  def insert(passId: String,
             fullName: String,
             nino: String,
             expirationDate: String,
             applePassCard: Array[Byte],
             qrCode: Array[Byte])
            (implicit ec: ExecutionContext): Future[Unit] = {
    logger.info(s"Inserted one in $collectionName table")
    collection.insertOne(ApplePass(passId, fullName, nino, expirationDate, applePassCard, qrCode))
      .toFuture().map(_ => ())
  }

  def findByPassId(passId: String)(implicit ec: ExecutionContext): Future[Option[ApplePass]] =
    collection.find(Filters.equal("passId", passId))
      .headOption()

  def findByNameAndNino(fullName: String, nino: String)(implicit ec: ExecutionContext): Future[Option[ApplePass]] =
    collection.find(
      Filters.and(
        Filters.equal("fullName", fullName),
        Filters.equal("nino", nino)
      )).headOption()

}