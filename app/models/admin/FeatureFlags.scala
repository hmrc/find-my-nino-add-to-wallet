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

package models.admin

import uk.gov.hmrc.mongoFeatureToggles.model.Environment.Environment
import uk.gov.hmrc.mongoFeatureToggles.model.{Environment, FeatureFlagName}

object AllFeatureFlags {
  val list: List[FeatureFlagName] = List(
    ApplePassCertificates2
  )
}

case object ApplePassCertificates2 extends FeatureFlagName {
  override val name: String                         = "apple-pass-certificates-2"
  override val description: Option[String]          = Some(
    "Switch to alternate Apple certificate values (applePass.*2) when enabled"
  )
  override val lockedEnvironments: Seq[Environment] = Seq(Environment.Production, Environment.Staging)
}
