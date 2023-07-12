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


import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.google.auth.oauth2.{GoogleCredentials, ServiceAccountCredentials}
import models.{GooglePassCard, GooglePassTextRow}
import googleModels.{GenericPrivatePass, Image, ImageUri, LocalizedString, TextModuleData, TranslatedString}

import java.io._
import java.security.interfaces.RSAPrivateKey
import java.util
import java.util._
import scala.jdk.CollectionConverters._
import collection.mutable._

// $COVERAGE-OFF$
class CreateGenericPrivatePass{

  val logoImageUrl = "https://raw.githubusercontent.com/hmrc/find-my-nino-add-to-wallet-frontend/main/public/images/hmrc-logo-google-pass.png"
  val logoImageDiscription = "HMRC"

  /**
   * Creates a JWT with the generic private pass encoded as JSON
   *
   * @param id              A unique identifier for the generic private pass
   * @param issuerId        A unique ID identifying your issuer account
   * @param key             The private key credentials
   * @param googlePassCard  The google pass card model containing the data to display on the card
   * @return The generated JWT string
   */

  /*def createJwt(id: String, issuerId: String, key: String, googlePassCard: GooglePassCard): String = try {
    createAndSignJWT(createGoogleCredentials(key), createGenericPrivatePassObject(id, issuerId, googlePassCard))
  } catch {
    case e: IOException =>
      throw new RuntimeException("Error saving JWT: " + e)
  }

  def createGoogleCredentials(key:String): GoogleCredentials = {
    val scope = "https://www.googleapis.com/auth/wallet_object.issuer"
    val keyAsStream = new ByteArrayInputStream(Base64.getDecoder.decode(key))
    val credentials: GoogleCredentials = GoogleCredentials.fromStream(keyAsStream).createScoped(Collections.singletonList(scope))
    credentials.refresh()
    credentials
  }*/

  def createJwtWithCredentials(id: String, issuerId: String, googlePassCard: GooglePassCard, googleCredentials: GoogleCredentials): String = try {
    createAndSignJWT(googleCredentials, createGenericPrivatePassObject(id, issuerId, googlePassCard))
  } catch {
    case e: IOException =>
      throw new RuntimeException("Error saving JWT: " + e)
  }

  /**
   * Creates the generic private pass object
   *
   * @param id              A unique identifier for the generic private pass
   * @param issuerId        A unique ID identifying your issuer account
   * @param googlePassCard  The google pass card model containing the data to display on the card
   * @return The created generic private pass
   */
  private def createGenericPrivatePassObject(id: String, issuerId: String, googlePassCard: GooglePassCard): GenericPrivatePass = {

    val imageUri = new ImageUri()
      .setDescription(logoImageDiscription)
      .setUri(logoImageUrl)
    val image = new Image().setSourceUri(imageUri)

    val textModulesData = ArrayBuffer[TextModuleData]()
    for (row: GooglePassTextRow <- googlePassCard.rows.get) {
      textModulesData += new TextModuleData()
        .setBody(row.body.getOrElse(""))
        .setId(row.id.getOrElse(""))
        .setHeader(row.header.getOrElse(""))
    }

    val translatedTitleString: TranslatedString = new TranslatedString()
      .setValue(googlePassCard.title)
      .setLanguage(googlePassCard.language)
    val title = new LocalizedString()
      .setDefaultValue(translatedTitleString)

    val translatedHeaderString = new TranslatedString()
      .setValue(googlePassCard.header)
      .setLanguage(googlePassCard.language)
    val header = new LocalizedString()
      .setDefaultValue(translatedHeaderString)

    new GenericPrivatePass()
      .setId(issuerId + "." + id)
      .setHeaderLogo(image)
      .setTitle(title)
      .setType("GENERIC_PRIVATE_PASS_TYPE_UNSPECIFIED")
      .setHeader(header)
      .setTextModulesData(textModulesData.asJava)
      .setHexBackgroundColor(googlePassCard.hexBackgroundColour)
  }




  /**
   * Creates and signs a JWT using a private key
   *
   * @param googleCredentials  The google credentials containing the private key for signing
   * @param genericPrivatePass The generic private pass object
   * @return The generated JWT string signed with a private key
   */
  private def createAndSignJWT(googleCredentials: GoogleCredentials, genericPrivatePass: GenericPrivatePass): String = {
    // Create the JWT as a HashMap object
    val claims: util.HashMap[String, Object] = new util.HashMap[String, Object]()

    claims.put("iss", googleCredentials.asInstanceOf[ServiceAccountCredentials].getClientEmail())
    claims.put("aud", "google")
    claims.put("origins", Collections.singletonList("www.example.com"))
    claims.put("typ", "savetowallet")
    // Create the Google Wallet payload and add to the JWT
    val payload: util.HashMap[String, Object] = new util.HashMap[String, Object]()
    payload.put("genericPrivatePasses", util.Arrays.asList(genericPrivatePass))
    claims.put("payload", payload)
    // The service account credentials are used to sign the JWT
    val algorithm: Algorithm = Algorithm.RSA256(null,
      (googleCredentials.asInstanceOf[ServiceAccountCredentials].getPrivateKey()).asInstanceOf[RSAPrivateKey])
    JWT.create.withPayload(claims).sign(algorithm)
  }
}
// $COVERAGE-ON$