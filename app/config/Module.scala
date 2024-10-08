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

package config

import com.google.inject.AbstractModule
import play.api.{Configuration, Environment}
import repositories.{ApplePassRepoTrait, ApplePassRepository, EncryptedApplePassRepository,
  EncryptedGooglePassRepository, GooglePassRepoTrait, GooglePassRepository}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.play.bootstrap.auth.DefaultAuthConnector

class Module(env: Environment, config: Configuration) extends AbstractModule {

  private val encryptionEnabled = config.get[Boolean]("mongodb.encryption.enabled")

  override def configure(): Unit = {

    bind(classOf[ApplicationStartUp]).asEagerSingleton()
    bind(classOf[AppConfig]).asEagerSingleton()
    bind(classOf[AuthConnector]).to(classOf[DefaultAuthConnector]).asEagerSingleton()

    if (encryptionEnabled) {
      bind(classOf[ApplePassRepoTrait]).to(classOf[EncryptedApplePassRepository]).asEagerSingleton()
      bind(classOf[GooglePassRepoTrait]).to(classOf[EncryptedGooglePassRepository]).asEagerSingleton()
    } else {
      bind(classOf[ApplePassRepoTrait]).to(classOf[ApplePassRepository]).asEagerSingleton()
      bind(classOf[GooglePassRepoTrait]).to(classOf[GooglePassRepository]).asEagerSingleton()
    }

  }
}
