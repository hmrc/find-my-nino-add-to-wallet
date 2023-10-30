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
import io.jsonwebtoken.{JwtException, Jwts}
import models.{CredentialSubject, GovUKVCDocument, Name, NameParts, SocialSecurityRecord, VCDocument}
import play.api.libs.json.Json
import services.googlepass.googleModels.GenericPrivatePass

import java.math.BigInteger
import java.security.{KeyFactory, PrivateKey, PublicKey}
import java.security.interfaces.{ECPrivateKey, RSAPrivateKey}
import java.security.spec.{ECFieldFp, ECParameterSpec, ECPoint, ECPublicKeySpec, EllipticCurve, PKCS8EncodedKeySpec, X509EncodedKeySpec}
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


  def createAndSignJWT(govUKVCDocument: GovUKVCDocument): String = {

    val pk = createECPrivateKeyFromBase64(config.govukVerificatonPrivateKey)

    val algorithm: Algorithm = Algorithm.ECDSA256(null, pk)

    val now = LocalDateTime.now(ZoneId.of("UTC"))
    val expiresAt = now.plusYears(config.govukPassdefaultExpirationYears).atZone(ZoneId.of("UTC")).toInstant.toEpochMilli

    val claims: util.Map[String, AnyRef] = new util.HashMap[String, AnyRef]()
    val payload: util.HashMap[String, Object] = new util.HashMap[String, Object]()

    payload.put("govUkPasses", util.Arrays.asList(Json.toJson(govUKVCDocument).toString))

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

  def verifyJwt(jwt: String): Boolean = {
    try {
      val publicKey = generatePublicKey(config.govukVerificatonPublicKeyX, config.govukVerificatonPublicKeyY)
      Jwts.parser().setSigningKey(publicKey).parseClaimsJws(jwt)
      true // Verification succeeded
    } catch {
      case e: JwtException =>
        // JWT verification failed
        false
    }
  }


  def generatePublicKey(x: String, y: String): PublicKey = {
    // Convert Base64-encoded x and y to BigInteger
    val xCoord = new BigInteger(1, java.util.Base64.getDecoder.decode(x))
    val yCoord = new BigInteger(1, java.util.Base64.getDecoder.decode(y))

    // Define the curve parameters for P-256
    val p = new BigInteger("ffffffff00000001000000000000000000000000ffffffffffffffffffffffff", 16)
    val a = new BigInteger("ffffffff00000001000000000000000000000000fffffffffffffffffffffffc", 16)
    val b = new BigInteger("5ac635d8aa3a93e7b3ebbd55769886bc651d06b0cc53b0f63bce3c3e27d2604b", 16)
    val curve = new EllipticCurve(new ECFieldFp(p), a, b)

    val ecSpec = new ECParameterSpec(
      curve,
      new ECPoint(
        new BigInteger("6b17d1f2e12c4247f8bce6e563a440f277037d812deb33a0f4a13945d898c296", 16),
        new BigInteger("4fe342e2fe1a7f9b8ee7eb4a7c0f9e162bce33576b315ececbb6406837bf51f5", 16)
      ),
      new BigInteger("ffffffff00000000ffffffffffffffffbce6faada7179e84f3b9cac2fc632551", 16),
      1
    )

    val pubKeySpec = new ECPublicKeySpec(new ECPoint(xCoord, yCoord), ecSpec)

    // Generate the public key
    val kf = KeyFactory.getInstance("EC")
    kf.generatePublic(pubKeySpec)
  }
}
