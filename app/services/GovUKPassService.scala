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

package services

import config.AppConfig
import play.api.Logging
import util.GovUKWalletHelper

import java.util.{Base64, UUID}
import javax.inject.Inject
import scala.concurrent.ExecutionContext

class GovUKPassService @Inject()(val config: AppConfig,
                                 val qrCodeService: QrCodeService,
                                 govUKWalletHelper: GovUKWalletHelper) extends Logging {

  def createGovUKPass(title: String,
                      givenName: String,
                      familyName: String,
                      nino: String)(implicit ec: ExecutionContext): Either[Exception, (String, String)] = {

    val uuid = UUID.randomUUID().toString
    val ninoStr = nino.trim.replace(" ", "")

    // ****create a NINO which cannot exist, as we need to use these for testing with Gov Wallet team,
    // *** this needs to be removed later
    val fakeNino = "QQ" + ninoStr.substring(2)

    //val giveNames = givenName.filterNot(_.isEmpty)
    val vcDocument = govUKWalletHelper.createGovUKVCDocument(title, givenName, familyName, fakeNino)
    val signedJWT = govUKWalletHelper.createAndSignJWT(vcDocument)
    val govUkWalletUrlWithJWT = s"${config.govukWalletUrl}$signedJWT"
    val govUkWalletUrlWithJWTQrCode = qrCodeService.createQRCode(govUkWalletUrlWithJWT).getOrElse(Array.emptyByteArray)
    val govUkWalletUrlWithJWTQrCodeBase64String = Base64.getEncoder.encodeToString(govUkWalletUrlWithJWTQrCode)

    //removing data persistance as we are not using it.
    //govUKPassRepository.insert(uuid, givenName, familyName, ninoStr, govUkWalletUrlWithJWT, govUkWalletUrlWithJWTQrCodeBase64String)

    Right(govUkWalletUrlWithJWT, govUkWalletUrlWithJWTQrCodeBase64String)
  }

}
