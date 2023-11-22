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

package config

import javax.inject.{Inject, Singleton}
import play.api.Configuration

@Singleton
class AppConfig @Inject()(config: Configuration) {

  val appName: String = config.get[String]("appName")
  val frontendServiceUrl: String = config.get[String]("frontendServiceUrl")
  val appleWWDRCA: String = config.get[String]("applePass.appleWWDRCA")
  val privateCertificate: String = config.get[String]("applePass.privateCertificate")
  val privateCertificatePassword: String = config.get[String]("applePass.privateCertificatePassword")
  val googleIssuerId: String = config.get[String]("googlePass.issuerId")
  val googleKey: String = config.get[String]("googlePass.key")
  val googleJWTExpiry: Int = config.get[Int]("googlePass.expiry")
  val googleAddUrl: String = config.get[String]("googlePass.url")
  val cacheTtl: Int = config.get[Int]("mongodb.timeToLiveInSeconds")

  val govukPassContext: List[String] = config.get[String]("govukpass.context").split(",").toList
  val govukPassSub: String = config.get[String]("govukpass.sub")
  val govukPassNbf: Int = config.get[Int]("govukpass.nbf")
  val govukPassIss: String = config.get[String]("govukpass.iss")
  val govukPassExp: Int = config.get[Int]("govukpass.exp")
  val govukPassIat: Int = config.get[Int]("govukpass.iat")
  val govukPassdefaultExpirationYears = config.get[Int]("govukpass.defaultExpirationYears")
  val govukVerificatonPrivateKey = config.get[String]("govukpass.govukVerificatonPrivateKey")
  val govukVerificatonPublicKeyX = config.get[String]("govukpass.govukVerificatonPublicKeyX")
  val govukVerificatonPublicKeyY = config.get[String]("govukpass.govukVerificatonPublicKeyY")
  val govukWalletJWTEncrypted = config.get[Boolean]("govukpass.govUkWalletJWTEncrypted")

  lazy val govukWalletEnabled = config.getOptional[Boolean]("features.govuk-wallet-enabled").getOrElse(false)

  val govukWalletUrl: String = config.get[String]("govukpass.govUkWalletUrl")
}
