/*
 * Copyright 2025 HM Revenue & Customs
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

package models.encryption

import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{Format, OFormat, __}
import uk.gov.hmrc.crypto.{EncryptedValue, SymmetricCryptoFactory}
import EncryptedValueFormat._
import models.apple.ApplePass
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

import java.time.Instant

case class EncryptedApplePass(
  passId: String,
  fullName: EncryptedValue,
  nino: EncryptedValue,
  applePassCard: EncryptedValue,
  qrCode: EncryptedValue,
  lastUpdated: Instant
)

object EncryptedApplePass {

  implicit val dateFormat: Format[Instant] = MongoJavatimeFormats.instantFormat

  val encryptedFormat: OFormat[EncryptedApplePass] =
    ((__ \ "passId").format[String]
      ~ (__ \ "fullName").format[EncryptedValue]
      ~ (__ \ "nino").format[EncryptedValue]
      ~ (__ \ "applePassCard").format[EncryptedValue]
      ~ (__ \ "qrCode").format[EncryptedValue]
      ~ (__ \ "lastUpdated").format[Instant])(
      EncryptedApplePass.apply,
      eap => Tuple6(eap.passId, eap.fullName, eap.nino, eap.applePassCard, eap.qrCode, eap.lastUpdated)
    )

  def encrypt(applePass: ApplePass, key: String): EncryptedApplePass = {
    def e(field: String): EncryptedValue =
      SymmetricCryptoFactory.aesGcmAdCrypto(key).encrypt(field, applePass.passId)

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
    def d(field: EncryptedValue): String =
      SymmetricCryptoFactory.aesGcmAdCrypto(key).decrypt(field, encryptedApplePass.passId)

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
