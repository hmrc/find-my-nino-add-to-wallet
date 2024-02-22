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

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.google.inject.Inject
import config.AppConfig
import models._
import models.govuk.{CredentialSubject, GovUKVCDocument, Name, NameParts, SocialSecurityRecord, VCDocument}
import play.api.Logging
import play.api.libs.json.Json

import java.math.BigInteger
import java.security.KeyFactory
import java.security.interfaces.{ECPrivateKey, ECPublicKey}
import java.security.spec.{ECFieldFp, ECParameterSpec, ECPoint, ECPublicKeySpec, EllipticCurve, PKCS8EncodedKeySpec}
import java.time.{LocalDateTime, ZoneId}
import java.util.Base64

class GovUKWalletHelper @Inject()(val config: AppConfig) extends Logging {

  def createGovUKVCDocument(title: String, givenName: String, familyName: String, nino: String): GovUKVCDocument = {

    val issuedAt = (System.currentTimeMillis()) / 1000

    val nameParts = List(
      NameParts("Title", title.toLowerCase.capitalize),
      NameParts("GivenName", givenName.toLowerCase.capitalize),
      NameParts("FamilyName", familyName.toLowerCase.capitalize)
    )
    val name = List(Name(nameParts))
    val socialSecurityRecord = List(SocialSecurityRecord(nino))
    val credentialSubject = CredentialSubject(name, socialSecurityRecord)
    val vcDocument = VCDocument(List("VerifiableCredential", "SocialSecurityCredential"), credentialSubject)
    GovUKVCDocument(
      config.govukPassContext,
      config.govukPassSub,
      issuedAt.toInt,
      config.govukPassIss,
      config.govukPassExp,
      issuedAt.toInt,
      vcDocument
    )
  }
  def createAndSignJWT(govUKVCDocument: GovUKVCDocument): String = {
    val privKey = createECPrivateKeyFromBase64(config.govukVerificatonPrivateKey)
    val pubKey = createECPublicKeyFromBase64Components(config.govukVerificatonPublicKeyX, config.govukVerificatonPublicKeyY)
    val algorithm: Algorithm = Algorithm.ECDSA256(pubKey, privKey)
    val expiresAt = LocalDateTime.now(ZoneId.of("UTC")).plusYears(config.govukPassdefaultExpirationYears).atZone(ZoneId.of("UTC")).toInstant
    JWT.create.withKeyId(config.govukVerificatonPublicKeyIDPrefix + config.govukVerificatonPublicKeyID)
      .withExpiresAt(expiresAt).withPayload(Json.toJson(govUKVCDocument).toString).sign(algorithm)
  }

  private def createECPrivateKeyFromBase64(base64PrivateKey: String): ECPrivateKey = {
    val keyBytes = Base64.getDecoder.decode(base64PrivateKey)
    val keySpec = new PKCS8EncodedKeySpec(keyBytes)
    val keyFactory = KeyFactory.getInstance("EC")
    keyFactory.generatePrivate(keySpec).asInstanceOf[ECPrivateKey]
  }

  def createECPublicKeyFromBase64Components(xComponentBase64: String, yComponentBase64: String): ECPublicKey = {
    // Decode Base64-encoded x and y coordinates
    val xBytes = Base64.getDecoder.decode(xComponentBase64)
    val yBytes = Base64.getDecoder.decode(yComponentBase64)

    // Parse x and y coordinates
    val xCoord = new BigInteger(1, xBytes)
    val yCoord = new BigInteger(1, yBytes)

    // Curve parameters for P-256 (change as needed)
    // Prime number p for the underlying finite field
    val p = new BigInteger("FFFFFFFF00000001000000000000000000000000FFFFFFFFFFFFFFFFFFFFFFFF", 16)

    //Coefficient a for curve equation: y^2 = x^3 + ax + b
    val a = new BigInteger("FFFFFFFF00000001000000000000000000000000FFFFFFFFFFFFFFFFFFFFFFFC", 16)

    //Coefficient b for curve equation: y^2 = x^3 + ax + b
    val b = new BigInteger("5AC635D8AA3A93E7B3EBBD55769886BC651D06B0CC53B0F63BCE3C3E27D2604B", 16)

    val curve = new ECParameterSpec(
      new EllipticCurve(new ECFieldFp(p), a, b),
      new ECPoint(
        new BigInteger("6B17D1F2E12C4247F8BCE6E563A440F277037D812DEB33A0F4A13945D898C296", 16),
        new BigInteger("4FE342E2FE1A7F9B8EE7EB4A7C0F9E162BCE33576B315ECECBB6406837BF51F5", 16)
      ),
      new BigInteger("FFFFFFFF00000000FFFFFFFFFFFFFFFFBCE6FAADA7179E84F3B9CAC2FC632551", 16),
      1
    )

    // Create the ECPublicKey using the parsed coordinates and curve parameters
    val pubKeySpec = new ECPublicKeySpec(new ECPoint(xCoord, yCoord), curve)
    val keyFactory = KeyFactory.getInstance("EC")
    keyFactory.generatePublic(pubKeySpec).asInstanceOf[ECPublicKey]
  }

}
