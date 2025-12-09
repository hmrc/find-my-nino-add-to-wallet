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

import com.google.inject.AbstractModule
import play.api.Logging
import services.*

class ApplePassModule extends AbstractModule with Logging {

  override def configure(): Unit = {
    val router = sys.props.getOrElse("application.router", "")

    if (router == "testOnlyDoNotUseInAppConf.Routes") {
      logger.warn("[ApplePassModule] Using StubApplePassService (testOnly router enabled)")
      bind(classOf[ApplePassService]).to(classOf[StubApplePassService]).asEagerSingleton()
    } else {
      logger.info("[ApplePassModule] Using RealApplePassService")
      bind(classOf[ApplePassService]).to(classOf[RealApplePassService]).asEagerSingleton()
    }
  }
}
