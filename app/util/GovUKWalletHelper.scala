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

package util

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.google.inject.Inject
import config.AppConfig
import models.{CredentialSubject, GovUKVCDocument, Name, NameParts, SocialSecurityRecord, VCDocument}
import services.googlepass.googleModels.GenericPrivatePass

import java.security.KeyFactory
import java.security.interfaces.RSAPrivateKey
import java.security.spec.PKCS8EncodedKeySpec
import java.time.{LocalDateTime, ZoneId}
import java.util
import java.util.{Base64, Collections, UUID}

class GovUKWalletHelper @Inject()(val config: AppConfig) {

  def createGovUKVCDocument(givenName :List[String], familyName: String, nino: String): GovUKVCDocument = {
    val nameParts = NameParts(givenName, familyName)
    val name = Name(nameParts)
    val socialSecurityRecord = SocialSecurityRecord(nino)
    val credentialSubject = CredentialSubject(name, socialSecurityRecord)
    val vcDocument = VCDocument(List("VerifiableCredential", "SocialSecurityCredential"), credentialSubject)
    GovUKVCDocument(
      config.govukPassSub,
      config.govukPassNbf,
      config.govukPassIss,
      config.govukPassExp,
      config.govukPassIat,
      vcDocument
    )
  }

  def privateKeyFromString(privateKeyString: String): RSAPrivateKey = {
    val privateKeyPEM = privateKeyString
      .replaceAll("\\n", "") // Remove newlines if present
      .replaceAll("-----BEGIN PRIVATE KEY-----", "")
      .replaceAll("-----END PRIVATE KEY-----", "")

    val keyBytes = Base64.getDecoder.decode(privateKeyPEM)

    val keySpec = new PKCS8EncodedKeySpec(keyBytes)
    val keyFactory = KeyFactory.getInstance("RSA")
    keyFactory.generatePrivate(keySpec).asInstanceOf[RSAPrivateKey]
  }

  private def createAndSignJWT(genericPrivatePass: GenericPrivatePass): String = {
    val privateKey: RSAPrivateKey =
    val algorithm: Algorithm = Algorithm.RSA256(null, privateKey)
    val now = LocalDateTime.now(ZoneId.of("UTC"))
    val expiresAt = now.plusYears(config.govukPassdefaultExpirationYears).atZone(ZoneId.of("UTC")).toInstant.toEpochMilli

    val issuedAt = now.atZone(ZoneId.of("UTC")).toInstant.toEpochMilli
    val jwtId = UUID.randomUUID().toString

    val claims: util.Map[String, AnyRef] = new util.HashMap[String, AnyRef]()

    // Create the Wallet payload and add to the JWT
    val payload: util.HashMap[String, Object] = new util.HashMap[String, Object]()

    payload.put("genericPrivatePasses", util.Arrays.asList(genericPrivatePass))

    claims.put("payload", payload)

    val JWTExpiryDate = java.util.Date.from(LocalDateTime.now().plusMinutes(expiresAt).atZone(ZoneId.systemDefault()).toInstant)
    // The service account credentials are used to sign the JWT
    val algorithm: Algorithm = Algorithm.RSA256(null, privateKey)

    JWT.create.withExpiresAt(JWTExpiryDate).withPayload(claims).sign(algorithm)
  }


}
