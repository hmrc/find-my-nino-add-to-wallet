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

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.google.auth.oauth2.{GoogleCredentials, ServiceAccountCredentials}
import config.AppConfig
import models.google.{GooglePassCard, GooglePassTextRow}
import services.googlepass.googleModels.*

import java.io.IOException
import java.security.interfaces.RSAPrivateKey
import java.time.{LocalDateTime, ZoneId}
import java.util
import java.util.{Collections, Date, HashMap}
import javax.inject.Inject
import scala.collection.mutable.ArrayBuffer
import scala.jdk.CollectionConverters.*

// $COVERAGE-OFF$
class CreateGenericPrivatePass @Inject() (config: AppConfig) {

  private val logoImageUrl         =
    "https://www.tax.service.gov.uk/save-your-national-insurance-number/assets/images/hmrc-logo-tudor-google-pass.png"
  private val logoImageDescription = "HMRC"

  def createJwtWithCredentials(
    id: String,
    issuerId: String,
    googlePassCard: GooglePassCard,
    googleCredentials: GoogleCredentials,
    expiry: Int
  ): String =
    try
      createAndSignJWT(googleCredentials, createGenericPrivatePassObject(id, issuerId, googlePassCard), expiry)
    catch {
      case e: IOException =>
        throw new RuntimeException("Error saving JWT: " + e)
    }

  /** Creates the generic private pass object
    *
    * @param id
    *   A unique identifier for the generic private pass
    * @param issuerId
    *   A unique ID identifying your issuer account
    * @param googlePassCard
    *   The google pass card model containing the data to display on the card
    * @return
    *   The created generic private pass
    */
  private def createGenericPrivatePassObject(
    id: String,
    issuerId: String,
    googlePassCard: GooglePassCard
  ): GenericPrivatePass = {

    val image = new Image().setSourceUri(
      new ImageUri()
        .setDescription(logoImageDescription)
        .setUri(logoImageUrl)
    )

    val textModulesData = ArrayBuffer[TextModuleData]()
    for (row: GooglePassTextRow <- googlePassCard.rows.get)
      textModulesData += new TextModuleData()
        .setBody(row.body.getOrElse(""))
        .setId(row.id.getOrElse(""))
        .setHeader(row.header.getOrElse(""))

    val title = new LocalizedString().setDefaultValue(
      new TranslatedString()
        .setValue(googlePassCard.title)
        .setLanguage(googlePassCard.language)
    )

    val header = new LocalizedString().setDefaultValue(
      new TranslatedString()
        .setValue(googlePassCard.header)
        .setLanguage(googlePassCard.language)
    )

    new GenericPrivatePass()
      .setId(issuerId + "." + id)
      .setHeaderLogo(image)
      .setTitle(title)
      .setType("GENERIC_PRIVATE_PASS_TYPE_UNSPECIFIED")
      .setHeader(header)
      .setTextModulesData(textModulesData.asJava)
      .setHexBackgroundColor(googlePassCard.hexBackgroundColour)
  }

  /** Creates and signs a JWT using a private key
    *
    * @param googleCredentials
    *   The google credentials containing the private key for signing
    * @param genericPrivatePass
    *   The generic private pass object
    * @return
    *   The generated JWT string signed with a private key
    */
  private def createAndSignJWT(
    googleCredentials: GoogleCredentials,
    genericPrivatePass: GenericPrivatePass,
    expiry: Int
  ): String = {
    val creds   = googleCredentials.asInstanceOf[ServiceAccountCredentials]
    val expires = Date.from(LocalDateTime.now().plusMinutes(expiry).atZone(ZoneId.systemDefault()).toInstant)

    val payload = new util.HashMap[String, Object]()
    payload.put("genericPrivatePasses", java.util.Arrays.asList(genericPrivatePass))

    val claims = new util.HashMap[String, Object]()
    claims.put("iss", creds.getClientEmail)
    claims.put("aud", "google")
    claims.put("origins", Collections.singletonList(config.googleOrigins))
    claims.put("typ", "savetowallet")
    claims.put("payload", payload)

    val algorithm = Algorithm.RSA256(
      null,
      creds.getPrivateKey.asInstanceOf[RSAPrivateKey]
    )

    JWT.create.withExpiresAt(expires).withPayload(claims).sign(algorithm)
  }
}
// $COVERAGE-ON$
