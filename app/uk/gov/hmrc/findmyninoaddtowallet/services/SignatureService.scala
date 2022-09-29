/*
 * Copyright 2022 HM Revenue & Customs
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

package uk.gov.hmrc.findmyninoaddtowallet.services

import org.bouncycastle.asn1.cms.{AttributeTable, CMSAttributes}
import org.bouncycastle.asn1.x509.Attribute
import org.bouncycastle.asn1.{ASN1EncodableVector, DERSet, DERUTCTime}
import org.bouncycastle.cert.jcajce.JcaCertStore
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder
import org.bouncycastle.cms.{CMSProcessableFile, CMSSignedDataGenerator, CMSTypedData, DefaultSignedAttributeTableGenerator}
import org.bouncycastle.jcajce.provider.asymmetric.x509.CertificateFactory
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.operator.jcajce.{JcaContentSignerBuilder, JcaDigestCalculatorProviderBuilder}

import java.io.{File, FileInputStream}
import java.nio.file.{Files, Path, Paths}
import java.security.cert.X509Certificate
import java.security.{KeyStore, PrivateKey, Security}
import java.util
import java.util.Date
import javax.inject.Inject
import scala.util.{Success, Try}

class SignatureService @Inject()() {

  import SignatureService._

  Security.addProvider(new BouncyCastleProvider)

  def createSignatureForPass(passPath: Path,
                             privateCertificatePath: String,
                             privateCertificatePassword: String,
                             appleWWDRCACertificatePath: String
                            ): Boolean = {
    val resultForCreateSignature = for {
      signInfo <- loadSigningInformation(privateCertificatePath, privateCertificatePassword, appleWWDRCACertificatePath)
      processableFile <- Try(new CMSProcessableFile(passPath.resolve(FileService.MANIFEST_JSON_FILE_NAME).toFile))
      signContent <- signManifestUsingContent(processableFile, signInfo)
      result <- Try(Files.write(passPath.resolve(SIGNATURE_FILE_NAME), signContent))
    } yield result

    resultForCreateSignature match {
      case Success(_) => true
      case _ => false
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

  private def loadSigningInformation(privateCertificatePath: String,
                                     privateCertificatePassword: String,
                                     appleWWDRCACertificatePath: String): Try[ApplePassSignInformation] = {
    for {
      appleCertificate <- loadX509Certificate(Paths.get(getClass.getResource(appleWWDRCACertificatePath).toURI).toFile)
      privateCertificate <- loadPKCS12File(Paths.get(getClass.getResource(privateCertificatePath).toURI).toFile, privateCertificatePassword)
    } yield ApplePassSignInformation(privateCertificate._2, privateCertificate._1, appleCertificate)
  }

  private def loadX509Certificate(certificateFile: File): Try[X509Certificate] = Try {
    val factory = new CertificateFactory()
    val certificate = factory.engineGenerateCertificate(new FileInputStream(certificateFile))
    certificate.asInstanceOf[X509Certificate]
  }

  private def loadPKCS12File(certificateFile: File, password: String): Try[(PrivateKey, X509Certificate)] = Try {
    val keyStore = KeyStore.getInstance("PKCS12")
    keyStore.load(new FileInputStream(certificateFile), password.toCharArray)

    val aliases = keyStore.aliases
    while (aliases.hasMoreElements) {
      val aliasName = aliases.nextElement
      val key = keyStore.getKey(aliasName, password.toCharArray)
      if (key.isInstanceOf[PrivateKey]) {
        val privateKey = key.asInstanceOf[PrivateKey]
        val cert = keyStore.getCertificate(aliasName)
        if (cert.isInstanceOf[X509Certificate]) {
          val certificate = cert.asInstanceOf[X509Certificate]
          return Success((privateKey, certificate))
        }
      }
    }
    throw new IllegalStateException("No valid key-certificate pair in the key store")
  }
}

object SignatureService {
  val SIGNATURE_FILE_NAME = "signature"
}

private case class ApplePassSignInformation(privateCertificate: X509Certificate, privateKey: PrivateKey, appleWWDRCACert: X509Certificate)
