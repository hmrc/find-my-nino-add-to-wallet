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

package repositories.encryption

import org.joda.time.DateTime
import play.api.libs.functional.syntax.{toFunctionalBuilderOps, unlift}
import play.api.libs.json.{Format, OFormat, __}
import models.ApplePass
import uk.gov.hmrc.crypto.{EncryptedValue, SymmetricCryptoFactory}
import repositories.encryption.EncryptedValueFormat._
import uk.gov.hmrc.mongo.play.json.formats.MongoJodaFormats

case class EncryptedApplePass(passId: String,
                              fullName: EncryptedValue,
                              nino: EncryptedValue,
                              applePassCard: EncryptedValue,
                              qrCode: EncryptedValue,
                              lastUpdated: DateTime)

object EncryptedApplePass {

  implicit val dateFormat: Format[DateTime] = MongoJodaFormats.dateTimeFormat

  val encryptedFormat: OFormat[EncryptedApplePass] = {
    ((__ \ "passId").format[String]
      ~ (__ \ "fullName").format[EncryptedValue]
      ~ (__ \ "nino").format[EncryptedValue]
      ~ (__ \ "applePassCard").format[EncryptedValue]
      ~ (__ \ "qrCode").format[EncryptedValue]
      ~ (__ \ "lastUpdated").format[DateTime]
      )(EncryptedApplePass.apply, unlift(EncryptedApplePass.unapply))
  }

  def encrypt(applePass: ApplePass, key: String): EncryptedApplePass = {
    def e(field: String): EncryptedValue = {
      SymmetricCryptoFactory.aesGcmAdCrypto(key).encrypt(field, applePass.passId)
    }

    EncryptedApplePass(
      passId = applePass.passId,
      fullName = e(applePass.fullName),
      nino = e(applePass.nino),
      applePassCard = e(applePass.applePassCard.mkString(",")),
      qrCode = e(applePass.qrCode.mkString(",")),
      lastUpdated = applePass.lastUpdated
    )
  }

  def decrypt(encryptedApplePass: EncryptedApplePass, key: String): ApplePass = {
    def d(field: EncryptedValue): String = {
      SymmetricCryptoFactory.aesGcmAdCrypto(key).decrypt(field, encryptedApplePass.passId)
    }

    ApplePass(
      passId = encryptedApplePass.passId,
      fullName = d(encryptedApplePass.fullName),
      nino = d(encryptedApplePass.nino),
      applePassCard = d(encryptedApplePass.applePassCard).split(",").map(_.toByte),
      qrCode = d(encryptedApplePass.qrCode).split(",").map(_.toByte),
      lastUpdated = encryptedApplePass.lastUpdated
    )
  }
}
