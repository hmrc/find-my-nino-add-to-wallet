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

package repositories.encryption

import play.api.libs.functional.syntax.{toFunctionalBuilderOps, unlift}
import play.api.libs.json.{OFormat, __}
import repositories.RowPersonDetails
import repositories.encryption.EncryptedValueFormat._
import uk.gov.hmrc.crypto.{EncryptedValue, SymmetricCryptoFactory}

case class EncryptedRowPersonDetails(detailsId: String,
                            fullName: EncryptedValue,
                            nino: EncryptedValue,
                            personDetails: EncryptedValue,
                            dateCreated: EncryptedValue,
                            lastUpdated: String)

object EncryptedRowPersonDetails {

  val encryptedFormat: OFormat[EncryptedRowPersonDetails] = {
    ((__ \ "detailsId").format[String]
      ~ (__ \ "fullName").format[EncryptedValue]
      ~ (__ \ "nino").format[EncryptedValue]
      ~ (__ \ "personDetails").format[EncryptedValue]
      ~ (__ \ "dateCreated").format[EncryptedValue]
      ~ (__ \ "lastUpdated").format[String]
      )(EncryptedRowPersonDetails.apply, unlift(EncryptedRowPersonDetails.unapply))
  }

  def encrypt(rowPersonDetails: RowPersonDetails, key: String): EncryptedRowPersonDetails = {
    def e(field: String): EncryptedValue = {
      SymmetricCryptoFactory.aesGcmAdCrypto(key).encrypt(field, rowPersonDetails.detailsId)
    }

    EncryptedRowPersonDetails(
      detailsId = rowPersonDetails.detailsId,
      fullName = e(rowPersonDetails.fullName),
      nino = e(rowPersonDetails.nino),
      personDetails = e(rowPersonDetails.personDetails),
      dateCreated = e(rowPersonDetails.dateCreated),
      lastUpdated = rowPersonDetails.lastUpdated
    )
  }

  def decrypt(encryptedRowPersonDetails: EncryptedRowPersonDetails, key: String): RowPersonDetails = {
    def d(field: EncryptedValue): String = {
      SymmetricCryptoFactory.aesGcmAdCrypto(key).decrypt(field, encryptedRowPersonDetails.detailsId)
    }

    RowPersonDetails(
      detailsId = encryptedRowPersonDetails.detailsId,
      fullName = d(encryptedRowPersonDetails.fullName),
      nino = d(encryptedRowPersonDetails.nino),
      personDetails = d(encryptedRowPersonDetails.personDetails),
      dateCreated = d(encryptedRowPersonDetails.dateCreated),
      lastUpdated = encryptedRowPersonDetails.lastUpdated
    )
  }
}
