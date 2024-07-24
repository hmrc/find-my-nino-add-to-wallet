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

import com.nimbusds.jose.util.X509CertUtils
import config.AppConfig
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import java.io.ByteArrayInputStream
import java.security.{KeyStore, PrivateKey}
import java.security.cert.{CertificateExpiredException, X509Certificate}
import java.time.{Duration, Instant}
import java.util.{Base64, Date}
import javax.inject.Inject
import scala.util.{Failure, Success, Try}

class CertificatesCheck @Inject()(cc: ControllerComponents, config: AppConfig) extends BackendController(cc) {
  def checkValidity: Action[AnyContent] = Action { implicit request =>
    val decodedPublicCertificate = Base64.getDecoder.decode(config.appleWWDRCA)
    val appleCertificate = X509CertUtils.parse(decodedPublicCertificate)
    val expiry = appleCertificate.getNotAfter
    Try(appleCertificate.checkValidity(Date.from(Instant.now().plus(Duration.ofDays(60L))))) match {
      case Success(_) => Ok("Cert will be valid " + expiry)
      case Failure(ex: CertificateExpiredException) =>
        Ok("Cert will expire in less than 60 days " + expiry)
      case Failure(ex) => throw ex
    }
  }

  def checkValidity2: Action[AnyContent] = Action { implicit request =>
    def isPrivateX509(keyStore: KeyStore, password: String)(alias: String) = {
      for {
        key <- Try(keyStore.getKey(alias, password.toCharArray).asInstanceOf[PrivateKey])
        cert <- Try(keyStore.getCertificate(alias).asInstanceOf[X509Certificate])
      } yield (key, cert)
    }

    val keyStore = KeyStore.getInstance("PKCS12")
    val decodedPrivateCertificate = Base64.getDecoder.decode(config.privateCertificate)
    keyStore.load(new ByteArrayInputStream(decodedPrivateCertificate), config.privateCertificatePassword.toCharArray)

    import scala.jdk.CollectionConverters._

    keyStore.aliases().asScala.toSeq
      .map(isPrivateX509(keyStore, config.privateCertificatePassword))
      .find(_.isSuccess)
      .getOrElse(Failure(new IllegalStateException("No valid key-certificate pair in the key store"))) match {
      case Success((_, cert)) =>
        val expiry = cert.getNotAfter
        Try(cert.checkValidity(Date.from(Instant.now().plus(Duration.ofDays(60L))))) match {
          case Success(_) => Ok("Cert will be valid " + expiry)
          case Failure(ex: CertificateExpiredException) =>
            Ok("Cert will expire in less than 60 days " + expiry)
          case Failure(ex) => throw ex
        }
      case Failure(ex) => InternalServerError(ex.getMessage)
    }
  }
}
