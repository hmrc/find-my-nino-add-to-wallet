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

import java.security.{KeyFactory, PrivateKey}
import java.security.interfaces.{ECPrivateKey, RSAPrivateKey}
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

  /*def privateKeyFromString(privateKeyString: String): RSAPrivateKey = {


    val keyBytes = Base64.getDecoder.decode(privateKeyString)

    val keySpec = new PKCS8EncodedKeySpec(keyBytes)
    val keyFactory = KeyFactory.getInstance("RSA")
    keyFactory.generatePrivate(keySpec).asInstanceOf[RSAPrivateKey]
  }*/

//replace the passed param below to use gov wallet pass object
  def createAndSignJWT(govUKVCDocument: GovUKVCDocument): String = {

    val pk = createECPrivateKeyFromBase64(config.govukPrivateKey)

    val algorithm: Algorithm = Algorithm.ECDSA256(null, pk)

    val now = LocalDateTime.now(ZoneId.of("UTC"))
    val expiresAt = now.plusYears(config.govukPassdefaultExpirationYears).atZone(ZoneId.of("UTC")).toInstant.toEpochMilli

    val claims: util.Map[String, AnyRef] = new util.HashMap[String, AnyRef]()
    val payload: util.HashMap[String, Object] = new util.HashMap[String, Object]()

    payload.put("govUkPasses", util.Arrays.asList(govUKVCDocument.vc))

    claims.put("payload", payload)

    val JWTExpiryDate = java.util.Date.from(LocalDateTime.now().plusMinutes(expiresAt).atZone(ZoneId.systemDefault()).toInstant)

    JWT.create.withExpiresAt(JWTExpiryDate).withPayload(claims).sign(algorithm)
  }

  def createECPrivateKeyFromBase64(base64PrivateKey: String): ECPrivateKey = {
    val keyBytes = Base64.getDecoder.decode(base64PrivateKey)
    val keySpec = new PKCS8EncodedKeySpec(keyBytes)
    val keyFactory = KeyFactory.getInstance("EC")
    keyFactory.generatePrivate(keySpec).asInstanceOf[ECPrivateKey]
  }

}
