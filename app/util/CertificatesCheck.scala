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

import com.nimbusds.jose.util.X509CertUtils
import config.AppConfig
import play.libs.F.Tuple

import java.io.ByteArrayInputStream
import java.security.{KeyStore, PrivateKey}
import java.security.cert.X509Certificate
import java.util
import java.util.{Base64, Date}
import javax.inject.Inject
import scala.collection.convert.ImplicitConversions.`collection AsScalaIterable`
import scala.jdk.CollectionConverters.CollectionHasAsScala
import scala.util.{Failure, Success, Try}

class CertificatesCheck @Inject()(config: AppConfig) {
  def getAppleWWDRCAExpiryDate: (Date, String, String) = {
    val decodedPublicCertificate = Base64.getDecoder.decode(config.appleWWDRCA)
    val appleCertificate = X509CertUtils.parse(decodedPublicCertificate)
    (appleCertificate.getNotAfter,
    appleCertificate.getIssuerX500Principal.getName,
    appleCertificate.getSubjectX500Principal.getName)
  }

  def getPrivateCertificateExpiryDate: Date = {
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
        cert.getNotAfter
      case Failure(ex) => throw ex
    }
  }
}
