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

import com.nimbusds.jose.util.X509CertUtils
import org.bouncycastle.asn1.cms.{AttributeTable, CMSAttributes}
import org.bouncycastle.asn1.x509.Attribute
import org.bouncycastle.asn1.{ASN1EncodableVector, DERSet, DERUTCTime}
import org.bouncycastle.cert.jcajce.JcaCertStore
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder
import org.bouncycastle.cms.{CMSProcessableByteArray, CMSSignedDataGenerator, CMSTypedData, DefaultSignedAttributeTableGenerator}
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.operator.jcajce.{JcaContentSignerBuilder, JcaDigestCalculatorProviderBuilder}
import play.api.Logging

import java.io.ByteArrayInputStream
import java.security.cert.X509Certificate
import java.security.{KeyStore, PrivateKey, Security}
import java.util
import java.util.{Base64, Date}
import javax.inject.Inject
import scala.util.Try

class SignatureService @Inject()() extends Logging {

  import SignatureService._

  Security.addProvider(new BouncyCastleProvider)


  def createSignatureForPass(passContent: List[FileAsBytes],
                             privateCertificate: String,
                             privateCertificatePassword: String,
                             appleWWDRCACertificate: String
                            ): FileAsBytes = {

    if (passContent.isEmpty) {
      FileAsBytes(SIGNATURE_FILE_NAME, Array.emptyByteArray)
    } else {
      val resultForCreateSignature = for {
        signInfo <- loadSigningInformation(privateCertificate, privateCertificatePassword, appleWWDRCACertificate)
        processableFileBytes <- Try(new CMSProcessableByteArray(passContent.last.content))
        signContent <- signManifestUsingContent(processableFileBytes, signInfo)
      } yield signContent

      FileAsBytes(SIGNATURE_FILE_NAME, resultForCreateSignature.getOrElse(Array.emptyByteArray))
    }
  }

  private def signManifestUsingContent(content: CMSTypedData, signInfo: ApplePassSignInformation): Try[Array[Byte]] = {
    Try {
      val signedDataGenerator = new CMSSignedDataGenerator

      // Sha1 Signer (We are using SHA1 to create Manifest file)
      val sha1Signer = new JcaContentSignerBuilder("SHA1withRSA")
        .setProvider(BouncyCastleProvider.PROVIDER_NAME)
        .build(signInfo.privateKey)

      val signedAttributes = new ASN1EncodableVector
      val signingAttribute = new Attribute(CMSAttributes.signingTime, new DERSet(new DERUTCTime(new Date)))
      signedAttributes.add(signingAttribute)

      // Create the signing table
      val signedAttributesTable = new AttributeTable(signedAttributes)

      // Create the table table generator that will added to the Signer builder
      val signedAttributeGenerator = new DefaultSignedAttributeTableGenerator(signedAttributesTable)
      val digestCalculateProvider = new JcaDigestCalculatorProviderBuilder().setProvider(BouncyCastleProvider.PROVIDER_NAME).build
      signedDataGenerator.addSignerInfoGenerator(
        new JcaSignerInfoGeneratorBuilder(digestCalculateProvider)
          .setSignedAttributeGenerator(signedAttributeGenerator)
          .build(sha1Signer, signInfo.privateCertificate)
      )

      val certList = new util.ArrayList[X509Certificate]
      certList.add(signInfo.appleWWDRCACert)
      certList.add(signInfo.privateCertificate)
      val certs = new JcaCertStore(certList)
      signedDataGenerator.addCertificates(certs)
      signedDataGenerator.generate(content, false).getEncoded
    }
  }

  private def loadSigningInformation(privateCertificate: String,
                                     privateCertificatePassword: String,
                                     appleWWDRCACertificate: String): Try[ApplePassSignInformation] = {
    val decodedPublicCertificate = Base64.getDecoder.decode(appleWWDRCACertificate)
    val decodedPrivateCertificate = Base64.getDecoder.decode(privateCertificate)

    for {
      appleCertificate <- loadX509Certificate(decodedPublicCertificate)
      privateCertificate <- loadPKCS12File(decodedPrivateCertificate, privateCertificatePassword)
    } yield ApplePassSignInformation(privateCertificate._2, privateCertificate._1, appleCertificate)
  }

  private def loadX509Certificate(publicCertificate: Array[Byte]): Try[X509Certificate] = Try {
    X509CertUtils.parse(publicCertificate)
  }

  private def loadPKCS12File(privateCertificate: Array[Byte], password: String): Try[(PrivateKey, X509Certificate)] = Try {
    val keyStore = KeyStore.getInstance("PKCS12")
    keyStore.load(new ByteArrayInputStream(privateCertificate), password.toCharArray)

    val aliases: util.Enumeration[String] = keyStore.aliases
    var keyPair: Option[(PrivateKey, X509Certificate)] = None

    while (aliases.hasMoreElements && keyPair.isDefined) {
      val aliasName = aliases.nextElement
      val key = keyStore.getKey(aliasName, password.toCharArray)
      if (key.isInstanceOf[PrivateKey]) {
        val cert = keyStore.getCertificate(aliasName)
        if (cert.isInstanceOf[X509Certificate]) {
          keyPair = Some(
            (
              key.asInstanceOf[PrivateKey],
              cert.asInstanceOf[X509Certificate]
            )
          )
        }
      }
    }

    keyPair match {
      case Some(pair) => pair
      case None       => throw new IllegalStateException("No valid key-certificate pair in the key store")
    }
  }
}

object SignatureService {
  val SIGNATURE_FILE_NAME = "signature"
  val MANIFEST_JSON_FILE_NAME = "manifest.json"

}

private case class ApplePassSignInformation(privateCertificate: X509Certificate, privateKey: PrivateKey, appleWWDRCACert: X509Certificate)
