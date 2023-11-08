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

package services

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.sun.tools.javac.util.Convert
import config.AppConfig
import org.bouncycastle.crypto.util.PublicKeyFactory
import play.api.Logging
import repositories.GovUKPassRepository
import services.googlepass.googleModels.GenericPrivatePass
import util.GovUKWalletHelper

import java.security.PublicKey
import java.security.interfaces.RSAPrivateKey
import java.time.{LocalDateTime, ZoneId}
import java.util
import java.util.{Base64, Collections, Date, UUID}
import javax.inject.Inject
import scala.concurrent.ExecutionContext

class GovUKPassService @Inject()(val config: AppConfig,
                                 govUKPassRepository: GovUKPassRepository,
                                 val qrCodeService: QrCodeService,
                                 govUKWalletHelper: GovUKWalletHelper) extends Logging {

  def createGovUKPass(givenName: List[String],
                      familyName: String,
                      nino: String)(implicit ec: ExecutionContext): Either[Exception, (String, String)] = {

    val uuid = UUID.randomUUID().toString
    val ninoStr = nino.trim.replace(" ", "")

    //create a NINO which cannot exist, as we need to use these for testing with Gov Wallet team, this needs to be removed later
    val fakeNino = "QQ" + ninoStr.substring(2)

    val giveNames = givenName.filterNot(_.isEmpty)

    // create a helper class to prepare VCDocument from input data
    val vcDocument = govUKWalletHelper.createGovUKVCDocument(giveNames, familyName, fakeNino)

    //create and sign JWT here
    val signedJWT = govUKWalletHelper.createAndSignJWT(vcDocument)

    val govUkWalletUrlWithJWT = s"${config.govukWalletUrl}/$signedJWT"
    val govUkWalletUrlWithJWTQrCode = qrCodeService.createQRCode(govUkWalletUrlWithJWT).getOrElse(Array.emptyByteArray)

    val base64String = Base64.getEncoder.encodeToString(govUkWalletUrlWithJWTQrCode)

    //we probably dont need to save names and personal number, as the JWT containes all the data
    govUKPassRepository.insert(uuid, giveNames, familyName, ninoStr, govUkWalletUrlWithJWT, base64String)
    //Right(uuid)

    Right(govUkWalletUrlWithJWT, base64String)
  }

}
