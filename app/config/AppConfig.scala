/*
 * Copyright 2022 HM Revenue & Customs
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
  val serviceUrl: String = config.get[String]("serviceUrl")
  val passPath: String = config.get[String]("applePass.passPath")
  val appleWWDRCA: String = config.get[String]("applePass.appleWWDRCA")
  val privateCertificate: String = config.get[String]("applePass.privateCertificate")
  val privateCertificatePassword: String = config.get[String]("applePass.privateCertificatePassword")
}
