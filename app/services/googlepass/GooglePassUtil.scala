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

package services.googlepass

import com.google.auth.oauth2.GoogleCredentials
import config.AppConfig
import models.google.{GooglePassCard, GooglePassTextRow}

import java.util.UUID
import javax.inject.Inject

class GooglePassUtil @Inject() (config: AppConfig, createGenericPrivatePass: CreateGenericPrivatePass) {

  val issuerId: String = config.googlePassIssuerId
  lazy val id: String  = s"hmrc-${UUID.randomUUID()}"

  val key: String                  = config.googlePassKey
  val expiry: Int                  = config.googlePassExpiryYears
  private val googleAddUrl: String = config.googlePassAddUrl

  def createGooglePassWithCredentials(name: String, nino: String, googleCredentials: GoogleCredentials): String = {
    val passCard = createGooglePassCardContent(name, nino)
    val jwt      = createGenericPrivatePass.createJwtWithCredentials(id, issuerId, passCard, googleCredentials, expiry)
    googleAddUrl + jwt
  }

  private def createGooglePassCardContent(name: String, nino: String): GooglePassCard =
    GooglePassCard(
      header = "HM Revenue & Customs",
      title = "National Insurance number",
      rows = Some(
        Array(
          GooglePassTextRow(Some("row2left"), Some("NAME"), Some(name)),
          GooglePassTextRow(Some("row3left"), Some("NATIONAL INSURANCE NUMBER"), Some(nino)),
          GooglePassTextRow(
            Some("row4left"),
            None,
            Some("This is not proof of your identity or your right to work in the UK.")
          ),
          GooglePassTextRow(Some("row5left"), None, None),
          GooglePassTextRow(
            Some("row6left"),
            Some("Your National Insurance number on a letter"),
            Some(
              "You can get a letter confirming your National Insurance number from your personal tax account.\n" +
                "To sign in, you’ll need to create or use an existing Government Gateway user ID and password.\n" +
                "<a href='https://www.tax.service.gov.uk/gg/sign-in?continue=/personal-account/national-insurance-summary/print-letter'>" +
                "https://www.tax.service.gov.uk/gg/sign-in?continue=/personal-account/national-insurance-summary/print-letter</a>"
            )
          ),
          GooglePassTextRow(
            Some("row7left"),
            None,
            Some("To help prevent identity fraud, only share your number when necessary.")
          ),
          GooglePassTextRow(
            Some("row8left"),
            Some("You'll need it when you:"),
            Some(
              "• start paid work\n• apply for a driving licence\n• apply for a student loan\n• register to vote\n• claim state benefits"
            )
          ),
          GooglePassTextRow(
            Some("row9left"),
            Some("Your National Insurance number is:"),
            Some(
              "• unique to you and never changes\n• not proof of your identity\n• not proof of your right to work in the UK"
            )
          ),
          GooglePassTextRow(
            Some("row10left"),
            Some("Find out more about National Insurance"),
            Some("<a href='https://www.gov.uk/national-insurance'>https://www.gov.uk/national-insurance</a>")
          )
        )
      ),
      hexBackgroundColour = "#008985",
      language = "en"
    )
}
