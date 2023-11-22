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
import config.AppConfig
import org.joda.time.{DateTime, DateTimeZone}
import org.mongodb.scala.model.{IndexModel, IndexOptions, Indexes}
import play.api.Logging
import play.api.libs.json.{Format, Json}
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import uk.gov.hmrc.mongo.play.json.formats.{MongoBinaryFormats, MongoJodaFormats}

import java.util.concurrent.TimeUnit
import scala.concurrent.{ExecutionContext, Future}

case class GovUKPass(passId: String,
                     givenName: List[String],
                     familyName: String,
                     nino: String,
                     vcDocument: String,
                     qrCode: String,
                     lastUpdated: DateTime)

object GovUKPass {
  def apply(passId: String, givenName: List[String], familyName: String, nino: String, vcDocument: String, qrCode: String): GovUKPass = {
    GovUKPass(passId, givenName, familyName, nino, vcDocument, qrCode, DateTime.now(DateTimeZone.UTC))
  }

  implicit val dateFormat: Format[DateTime] = MongoJodaFormats.dateTimeFormat
  implicit val arrayFormat: Format[Array[Byte]] = MongoBinaryFormats.byteArrayFormat
  implicit val mongoFormat: Format[GovUKPass] = Json.format[GovUKPass]
}

@Singleton
class GovUKPassRepository @Inject()(mongoComponent: MongoComponent,
                                     appConfig: AppConfig
                                   )(implicit ec: ExecutionContext) extends PlayMongoRepository[GovUKPass](
  collectionName = "govuk-pass",
  mongoComponent = mongoComponent,
  domainFormat = GovUKPass.mongoFormat,
  indexes = Seq(
    IndexModel(
      Indexes.ascending("passId"),
      IndexOptions().name("passId").unique(true)
    ),
    IndexModel(
      Indexes.ascending("familyName", "nino"),
      IndexOptions().name("familyName_Nino")
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
             givenName: List[String],
             familyName: String,
             nino: String,
             vcDocument: String,
             qrCode: String)
            (implicit ec: ExecutionContext): Future[Unit] = {
    logger.info(s"Inserted one in $collectionName table")
    collection.insertOne(GovUKPass(passId, givenName, familyName, nino, vcDocument, qrCode))
      .toFuture().map(_ => ())
  }



}