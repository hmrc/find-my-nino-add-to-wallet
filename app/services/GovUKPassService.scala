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
import config.AppConfig
import play.api.Logging
import repositories.GovUKPassRepository
import services.googlepass.googleModels.GenericPrivatePass
import util.GovUKWalletHelper

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
                      nino: String)(implicit ec: ExecutionContext): Either[Exception, String] = {

    val uuid = UUID.randomUUID().toString
    val personalNumber = nino.trim.replace(" ", "")
    val giveNames = givenName.filterNot(_.isEmpty)

    // create a helper class to prepare VCDocument from input data
    val vcDocument = govUKWalletHelper.createGovUKVCDocument(giveNames, familyName, personalNumber)
    //val encodedVCDocument: String = Base64.getEncoder.encodeToString(vcDocument.toString.getBytes)

    //create and sign JWT here
    val signedJWT = govUKWalletHelper.createAndSignJWT(vcDocument)

    if(govUKWalletHelper.verifyJwt(signedJWT))
          println("****************JWT verified**************")
    else
        println("*****************JWT not verified***************")


    val qrCode = qrCodeService
      .createQRCode(s"${config.frontendServiceUrl}/get-govuk-pass?passId=$uuid&qr-code=true")
      .getOrElse(Array.emptyByteArray)

    //we probably dont need to save names and personal number, as the JWT containes all the data
    govUKPassRepository.insert(uuid, giveNames, familyName, personalNumber, signedJWT, qrCode)
    Right(uuid)
  }

}
