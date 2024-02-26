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

package models.apple

import services.ApplePassService._

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
  sharingProhibited : Boolean,
  generic: ApplePassGeneric
)

object ApplePassCard {
  val FORMAT_VERSION = 1
  val PASS_TYPE_IDENTIFIER = "pass.uk.gov.hmrc.sca.nino"
  val TEAM_IDENTIFIER = "4LL5YKZZU7"
  val ORGANIZATION_NAME = "HMRC"
  val DESCRIPTION = "National Insurance number"
  val LOGO_TEXT = "HM Revenue & Customs"
  val COLOR_WHITE = "rgb(255, 255, 255)"
  val COLOR_HMRC_GRAY = "rgb(0, 137, 133)"
  val SHARING_PROHIBITED = true

  def apply(fullName: String, nino: String, uuid: String): ApplePassCard = {
    val generic = ApplePassGeneric(
      Array(ApplePassField(KEY_NINO, Some(LABEL_NINO), nino)),
      Array(ApplePassField(KEY_NAME, Some(LABEL_NAME), fullName)),
      Array(ApplePassField(KEY_WARNING, None, TEXT_WARNING)),
      Array(
        ApplePassField("downloadpasspdf", Some("Your National Insurance number on a letter"), "You can get a letter confirming your National Insurance number from your personal tax account.\n" +
          "To sign in, you’ll need to create or use an existing Government Gateway user ID and password.\n" +
          "https://www.tax.service.gov.uk/gg/sign-in?continue=/personal-account/national-insurance-summary/print-letter"),
        ApplePassField("warning", None, "To help prevent identity fraud, only share your number when necessary."),
        ApplePassField("steps", Some("You'll need it when you:"), "• start paid work\n• apply for a driving licence\n• apply for a student loan\n• register to vote\n• claim state benefits"),
        ApplePassField("info", Some("Your National Insurance number is:"), "• unique to you and never changes\n• not proof of your identity\n• not proof of your right to work in the UK"),
        ApplePassField("findoutmore", Some("Find out more about National Insurance"), "https://www.gov.uk/national-insurance")
      )
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
      ApplePassCard.SHARING_PROHIBITED,
      generic
    )
  }

}
