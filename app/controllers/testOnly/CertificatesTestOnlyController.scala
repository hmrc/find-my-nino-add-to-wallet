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

package controllers.testOnly

import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.twirl.api.Html
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import util.CertificatesCheck

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class CertificatesTestOnlyController @Inject() (cc: MessagesControllerComponents, certificatesCheck: CertificatesCheck)(
  implicit ec: ExecutionContext
) extends BackendController(cc) {

  def showExpiry: Action[AnyContent] = Action.async {

    for {
      appleWWDRCAExpiry        <- certificatesCheck.getAppleWWDRCADetails
      privateCertificateExpiry <- certificatesCheck.getPrivateCertificateDetails
    } yield Ok(Html(s"""
           |apple WWDRCA expiry date: ${appleWWDRCAExpiry._1}
           |IssuerX500Principal: ${appleWWDRCAExpiry._2}
           |SubjectX500Principal: ${appleWWDRCAExpiry._3}
           |
           |private apple certificate expiry date: ${privateCertificateExpiry._1}
           |IssuerX500Principal: ${privateCertificateExpiry._2}
           |SubjectX500Principal: ${privateCertificateExpiry._3}
           |"""".stripMargin))
  }
}
