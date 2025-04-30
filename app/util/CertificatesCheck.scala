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

import java.io.ByteArrayInputStream
import java.security.{KeyStore, PrivateKey}
import java.security.cert.X509Certificate
import java.util.{Base64, Date}
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

@Singleton
class CertificatesCheck @Inject() (config: AppConfig)(implicit ec: ExecutionContext) {
  def getAppleWWDRCADetails: Future[(Date, String, String)] =
    config.appleWWDRCA.map { appleWWDRCA =>
      val decodedPublicCertificate = Base64.getDecoder.decode(appleWWDRCA)
      val appleCertificate         = X509CertUtils.parse(decodedPublicCertificate)
      (
        appleCertificate.getNotAfter,
        appleCertificate.getIssuerX500Principal.getName,
        appleCertificate.getSubjectX500Principal.getName
      )
    }

  def getPrivateCertificateDetails: Future[(Date, String, String)] = {
    def isPrivateX509(keyStore: KeyStore, password: String)(alias: String) =
      for {
        key  <- Try(keyStore.getKey(alias, password.toCharArray).asInstanceOf[PrivateKey])
        cert <- Try(keyStore.getCertificate(alias).asInstanceOf[X509Certificate])
      } yield (key, cert)

    for {
      privateCertificate         <- config.privateCertificate
      privateCertificatePassword <- config.privateCertificatePassword
    } yield {

      val keyStore                  = KeyStore.getInstance("PKCS12")
      val decodedPrivateCertificate = Base64.getDecoder.decode(privateCertificate)
      keyStore.load(new ByteArrayInputStream(decodedPrivateCertificate), privateCertificatePassword.toCharArray)

      import scala.jdk.CollectionConverters._

      keyStore
        .aliases()
        .asScala
        .toSeq
        .map(isPrivateX509(keyStore, privateCertificatePassword))
        .find(_.isSuccess)
        .getOrElse(Failure(new IllegalStateException("No valid key-certificate pair in the key store"))) match {
        case Success((_, cert)) =>
          (cert.getNotAfter, cert.getIssuerX500Principal.getName, cert.getSubjectX500Principal.getName)
        case Failure(ex)        => throw ex
      }
    }
  }
}
