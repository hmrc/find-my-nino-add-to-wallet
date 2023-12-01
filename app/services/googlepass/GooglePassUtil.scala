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

package services.googlepass

import com.google.auth.oauth2.GoogleCredentials
import config.AppConfig
import models.{GooglePassCard, GooglePassTextRow}

import java.util.UUID
import javax.inject.Inject

class GooglePassUtil @Inject()(config: AppConfig, createGenericPrivatePass: CreateGenericPrivatePass) {

  val issuerId: String = config.googleIssuerId
  val id: String = s"hmrc-${UUID.randomUUID()}"
  val key: String = config.googleKey
  val expiry: Int = config.googleJWTExpiry
  val googleAddUrl: String = config.googleAddUrl


  def createGooglePassWithCredentials(name: String, nino: String, googleCredentials: GoogleCredentials): String = {

    val googlePassCardContent = createGooglePassCardContent(name, nino)
    val jwt = createGenericPrivatePass.createJwtWithCredentials(id, issuerId, googlePassCardContent, googleCredentials, expiry)
    val saveUrl = googleAddUrl + jwt
    saveUrl
  }

  private def createGooglePassCardContent(name: String, nino: String): GooglePassCard = {
    val pass: GooglePassCard = GooglePassCard(
      header = "HM Revenue & Customs",
      title = "National Insurance number",
      rows = Some(Array(
        GooglePassTextRow(
          id = Some("row2left"),
          header = Some("NAME"),
          body = Some(name)),
        GooglePassTextRow(
          id = Some("row3left"),
          header = Some("NATIONAL INSURANCE NUMBER"),
          body = Some(nino)),
        GooglePassTextRow(
          id = Some("row4left"),
          header = None,
          body = Some("This is not proof of your identity or your right to work in the UK.")),
        GooglePassTextRow(
          id = Some("row5left"),
          header = None,
          body = None),
        GooglePassTextRow(
          id = Some("row6left"),
          header = Some("Your National Insurance number on a letter"),
          body = Some("You can get a letter confirming your National Insurance number from your personal tax account.\n" +
            "To sign in, you’ll need to create or use an existing Government Gateway user ID and password.\n" +
            "<a href='https://www.tax.service.gov.uk/gg/sign-in?continue=/personal-account/national-insurance-summary/print-letter'>" +
            "https://www.tax.service.gov.uk/gg/sign-in?continue=/personal-account/national-insurance-summary/print-letter" +
            "</a>")),
        GooglePassTextRow(
          id = Some("row7left"),
          header = None,
          body = Some("To help prevent identity fraud, only share your number when necessary.")),
        GooglePassTextRow(
          id = Some("row8left"),
          header = Some("You'll need it when you:"),
          body = Some("• start paid work\n• apply for a driving licence\n• apply for a student loan\n• register to vote\n• claim state benefits")),
        GooglePassTextRow(
          id = Some("row9left"),
          header = Some("Your National Insurance number is:"),
          body = Some("• unique to you and never changes\n• not proof of your identity\n• not proof of your right to work in the UK")),
        GooglePassTextRow(
          id = Some("row10left"),
          header = Some("Find out more about National Insurance"),
          body = Some("<a href='https://www.gov.uk/national-insurance'>" +
            "https://www.gov.uk/national-insurance" +
            "</a>"))
        )),
      hexBackgroundColour = "#008985",
      language = "en"
    )
    pass
  }
}