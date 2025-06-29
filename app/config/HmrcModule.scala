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

package config

import play.api.{Configuration, Environment}
import play.api.inject.{Binding, Module}
import repositories.*
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.play.bootstrap.auth.DefaultAuthConnector

class HmrcModule extends Module {

  override def bindings(environment: Environment, configuration: Configuration): Seq[Binding[_]] = {
    val encryptionEnabled = configuration.get[Boolean]("mongodb.encryption.enabled")

    Seq(
      bind[ApplicationStartUp].toSelf.eagerly(),
      bind[AuthConnector].to(classOf[DefaultAuthConnector])
    ) ++ {
      if (encryptionEnabled) {
        Seq(
          bind[ApplePassRepoTrait].to(classOf[EncryptedApplePassRepository]),
          bind[GooglePassRepoTrait].to(classOf[EncryptedGooglePassRepository])
        )
      } else {
        Seq(
          bind[ApplePassRepoTrait].to(classOf[ApplePassRepository]),
          bind[GooglePassRepoTrait].to(classOf[GooglePassRepository])
        )
      }
    }
  }
}
