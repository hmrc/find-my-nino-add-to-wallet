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

package models.google

import play.api.libs.json.{Format, Json}
import uk.gov.hmrc.mongo.play.json.formats.{MongoBinaryFormats, MongoJavatimeFormats}

import java.time.Instant

case class GooglePass(
  passId: String,
  fullName: String,
  nino: String,
  expirationDate: String,
  googlePassUrl: String,
  qrCode: Array[Byte],
  lastUpdated: Instant
)

object GooglePass {
  def apply(
    passId: String,
    fullName: String,
    nino: String,
    expirationDate: String,
    googlePassUrl: String,
    qrCode: Array[Byte]
  ): GooglePass =
    GooglePass(passId, fullName, nino, expirationDate: String, googlePassUrl, qrCode, Instant.now)

  implicit val dateFormat: Format[Instant]      = MongoJavatimeFormats.instantFormat
  implicit val arrayFormat: Format[Array[Byte]] = MongoBinaryFormats.byteArrayFormat
  implicit val mongoFormat: Format[GooglePass]  = Json.format[GooglePass]
}
