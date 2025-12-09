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

import org.scalatest.BeforeAndAfter
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import services.{GooglePassService, RealGooglePassService, StubGooglePassService}

class GooglePassModuleSpec extends AnyWordSpec with Matchers with BeforeAndAfter {

  private val baseConfig: Map[String, Any] =
    Map(
      "auditing.enabled" -> false,
      "metrics.enabled"  -> false,
      "metrics.jvm"      -> false
    )

  before {
    sys.props -= "application.router"
  }

  after {
    sys.props -= "application.router"
  }

  "GooglePassModule" must {

    "bind StubGooglePassService when testOnly router is enabled" in {
      sys.props += "application.router" -> "testOnlyDoNotUseInAppConf.Routes"

      val application: Application =
        new GuiceApplicationBuilder()
          .configure(baseConfig)
          .overrides(new GooglePassModule)
          .build()

      val service = application.injector.instanceOf[GooglePassService]

      service mustBe a[StubGooglePassService]
    }

    "bind RealGooglePassService when testOnly router is not enabled" in {
      sys.props += "application.router" -> "app.Routes"

      val application: Application =
        new GuiceApplicationBuilder()
          .configure(baseConfig)
          .overrides(new GooglePassModule)
          .build()

      val service = application.injector.instanceOf[GooglePassService]

      service mustBe a[RealGooglePassService]
    }

    "bind RealGooglePassService when router system property is missing" in {
      val application: Application =
        new GuiceApplicationBuilder()
          .configure(baseConfig)
          .overrides(new GooglePassModule)
          .build()

      val service = application.injector.instanceOf[GooglePassService]

      service mustBe a[RealGooglePassService]
    }
  }
}
