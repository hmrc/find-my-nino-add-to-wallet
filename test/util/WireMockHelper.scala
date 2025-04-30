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

package util

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, Suite}
trait WireMockHelper extends BeforeAndAfterAll with BeforeAndAfterEach {
  this: Suite =>

  val wireHost: String       = "localhost"
  val server: WireMockServer = new WireMockServer(wireMockConfig().dynamicPort())
  lazy val wirePort: Int     = server.port()

  override def beforeAll(): Unit = {
    server.start()
    super.beforeAll()
  }

  override def beforeEach(): Unit = {
    server.resetAll()
    super.beforeEach()
  }

  override def afterAll(): Unit = {
    super.afterAll()
    server.stop()
  }

  def overrideConfig: Map[String, Any] =
    Map(
      "auditing.enabled"                                          -> false,
      "metrics.enabled"                                           -> false,
      "microservice.services.nps-crn-api.correlationId.key"       -> "test",
      "microservice.services.nps-crn-api.govUkOriginatorId.key"   -> "test",
      "microservice.services.nps-crn-api.govUkOriginatorId.value" -> "test",
      "microservice.services.nps-crn-api.protocol"                -> "http",
      "microservice.services.nps-crn-api.host"                    -> wireHost,
      "microservice.services.nps-crn-api.port"                    -> wirePort,
      "microservice.services.nps-crn-api.token"                   -> "test"
    )
}
