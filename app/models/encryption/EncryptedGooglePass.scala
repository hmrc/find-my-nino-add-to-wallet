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

package models.encryption

import models.google.GooglePass
import play.api.libs.functional.syntax.{toFunctionalBuilderOps, unlift}
import play.api.libs.json.{Format, OFormat, __}
import uk.gov.hmrc.crypto.{EncryptedValue, SymmetricCryptoFactory}
import EncryptedValueFormat._
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

import java.time.Instant

case class EncryptedGooglePass(passId: String,
                               fullName: EncryptedValue,
                               nino: EncryptedValue,
                               expirationDate: EncryptedValue,
                               googlePassUrl: EncryptedValue,
                               qrCode: EncryptedValue,
                               lastUpdated: Instant)

object EncryptedGooglePass {

  implicit val dateFormat: Format[Instant] = MongoJavatimeFormats.instantFormat

  val encryptedFormat: OFormat[EncryptedGooglePass] = {
    ((__ \ "passId").format[String]
      ~ (__ \ "fullName").format[EncryptedValue]
      ~ (__ \ "nino").format[EncryptedValue]
      ~ (__ \ "expirationDate").format[EncryptedValue]
      ~ (__ \ "googlePassUrl").format[EncryptedValue]
      ~ (__ \ "qrCode").format[EncryptedValue]
      ~ (__ \ "lastUpdated").format[Instant]
      )(EncryptedGooglePass.apply, unlift(EncryptedGooglePass.unapply))
  }

  def encrypt(googlePass: GooglePass, key: String): EncryptedGooglePass = {
    def e(field: String): EncryptedValue = {
      SymmetricCryptoFactory.aesGcmAdCrypto(key).encrypt(field, googlePass.passId)
    }

    EncryptedGooglePass(
      passId = googlePass.passId,
      fullName = e(googlePass.fullName),
      nino = e(googlePass.nino),
      expirationDate = e(googlePass.expirationDate),
      googlePassUrl = e(googlePass.googlePassUrl),
      qrCode = e(googlePass.qrCode.mkString(",")),
      lastUpdated = googlePass.lastUpdated
    )
  }

  def decrypt(encryptedGooglePass: EncryptedGooglePass, key: String): GooglePass = {
    def d(field: EncryptedValue): String = {
      SymmetricCryptoFactory.aesGcmAdCrypto(key).decrypt(field, encryptedGooglePass.passId)
    }

    GooglePass(
      passId = encryptedGooglePass.passId,
      fullName = d(encryptedGooglePass.fullName),
      nino = d(encryptedGooglePass.nino),
      expirationDate = d(encryptedGooglePass.expirationDate),
      googlePassUrl = d(encryptedGooglePass.googlePassUrl),
      qrCode = d(encryptedGooglePass.qrCode).split(",").map(_.toByte),
      lastUpdated = encryptedGooglePass.lastUpdated
    )
  }
}
