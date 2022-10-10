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

package models

import services.ApplePassService.{KEY_NAME, KEY_NINO, KEY_WARNING, LABEL_NAME, LABEL_NINO, TEXT_WARNING}

case class ApplePassCard
(
  formatVersion: Int,
  passTypeIdentifier: String,
  teamIdentifier: String,
  organizationName: String,
  serialNumber: String,
  description: String,
  logoText: String,
  foregroundColor: String,
  backgroundColor: String,
  labelColor: String,
  generic: ApplePassGeneric
)

object ApplePassCard {
  val FORMAT_VERSION = 1
  val PASS_TYPE_IDENTIFIER = "pass.uk.gov.hmrc.sca.nino"
  val TEAM_IDENTIFIER = "4LL5YKZZU7"
  val ORGANIZATION_NAME = "HMRC"
  val DESCRIPTION = "National Insurance Number Card"
  val LOGO_TEXT = "National Insurance Number Card"
  val COLOR_WHITE = "rgb(255, 255, 255)"
  val COLOR_HMRC_GRAY = "rgb(0, 137, 133)"

  def apply(fullName: String, nino: String, uuid: String): ApplePassCard = {
    val generic = ApplePassGeneric(
      Array(ApplePassField(KEY_NINO, Some(LABEL_NINO), nino)),
      Array(ApplePassField(KEY_NAME, Some(LABEL_NAME), fullName)),
      Array(ApplePassField(KEY_WARNING, None, TEXT_WARNING))
    )

    ApplePassCard(
      ApplePassCard.FORMAT_VERSION,
      ApplePassCard.PASS_TYPE_IDENTIFIER,
      ApplePassCard.TEAM_IDENTIFIER,
      ApplePassCard.ORGANIZATION_NAME,
      uuid,
      ApplePassCard.DESCRIPTION,
      ApplePassCard.LOGO_TEXT,
      ApplePassCard.COLOR_WHITE,
      ApplePassCard.COLOR_HMRC_GRAY,
      ApplePassCard.COLOR_WHITE,
      generic
    )
  }

}
