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

import models.apple.{ApplePassCard, ApplePassField, ApplePassGeneric}
import org.mockito.MockitoSugar
import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatest.wordspec.AsyncWordSpec
import play.api.libs.json.{Json, OFormat}

import java.util.UUID

class FileServiceSpec extends AsyncWordSpec with Matchers with MockitoSugar {
  import services.FileServiceSpec._

  val PASS_FILE_NAME = "pass.json"
  val ICON_FILE_NAME = "icon.png"
  val LOGO_FILE_NAME = "logo.png" //top of the card logo
  val MANIFEST_JSON_FILE_NAME = "manifest.json"
  val ICON_RESOURCE_PATH = s"/resources/pass/$ICON_FILE_NAME"
  val LOGO_RESOURCE_PATH = s"/resources/pass/$LOGO_FILE_NAME"
  val privateKey = "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"
  val publicKey = "AAAAB3NzaC1kc3MAAAEBAI95Ndm5qum/q+2Ies9JUbbzLsWeO683GOjqxJYfPv02BudDUanEGDM5uAnnwq4cU5unR1uF0BGtuLR5h3VJhGlcrA6PFLM2CCiiL/onEQo9YqmTRTQJoP5pbEZY+EvdIIGcNwmgEFexla3NACM9ulSEtikfnWSO+INEhneXnOwEtDSmrC516Zhd4j2wKS/BEYyf+p2BgeczjbeStzDXueNJWS9oCZhyFTkV6j1ri0ZTxjNFj4A7MqTC4PJykCVuTj+KOwg4ocRQ5OGMGimjfd9eoUPeS2b/BJA+1c8WI+FY1IfGCOl/IRzYHcojy244B2X4IuNCvkhMBXY5OWAc1mcAAAAdALr2lqaFePff3uf6Z8l3x4XvMrIzuuWAwLzVaV0AAAEAFqZcWCBIUHBOdQKjl1cEDTTaOjR4wVTU5KXALSQu4E+W5h5L0JBKvayPN+6x4J8xgtI8kEPLZC+IAEFg7fnKCbMgdqecMqYn8kc+kYebosTnRL0ggVRMtVuALDaNH6g+1InpTg+gaI4yQopceMR4xo0FJ7ccmjq7CwvhLERoljnn08502xAaZaorh/ZMaCbbPscvS1WZg0u07bAvfJDppJbTpV1TW+v8RdT2GfY/Pe27hzklwvIk4HcxKW2oh+weR0j4fvtf3rdUhDFrIjLe5VPdrwIRKw0fAtowlzIk/ieu2oudSyki2bqL457Z4QOmPFKBC8aIt+LtQxbh7xfb3gAAAQAtMYLAiKqWBj5lr3LwTo92qN0fqzhN62doz+VxGJJk8QcnpaP3Pw+TGC11Eb3o2WyYtNVxIJL31LQSVBEAZz0uy03sBQpoEkLiMRrfSShGALBVGSYAyEdm+dtJmK+x3YS2kPFY/jCu9QXTdbZ6akcp+fTP/7ecLqDA5moW2wMqR7mMG1TE8UfW/wbE6Om/hbnKAGdKL2GzCRDAj5fLVr3GBZZqA6b+XWlNEkzsLdckgdMP5Ug6BbJsm/ik82z10gEQvr/IKWm+xq4YVOBcsTxONHk3Mzox/s9e/J3J0aQLuZqtkF+vuj22PyFuD15C/ZNzQNFXW3ibFv/euG6Iawtz"


  "fileService" must {
    val uuid = UUID.randomUUID().toString
    val applePass = ApplePassCard(
      "Test Pass", "AB 12 34 56 Q",
      uuid)

    "create test.pass with icon, thumbnail, pass.json, manifest files as bytes" in {
      val passDirectoryCreated = fileService.createFileBytesForPass(applePass)
      passDirectoryCreated.nonEmpty mustBe true
      passDirectoryCreated.size mustBe 4

      passDirectoryCreated.head.filename mustBe PASS_FILE_NAME
      passDirectoryCreated.head.content.length should be > 1

      passDirectoryCreated.lift(1).get.filename mustBe ICON_FILE_NAME
      passDirectoryCreated.last.content.length should be > 1

      passDirectoryCreated.lift(2).get.filename mustBe LOGO_FILE_NAME
      passDirectoryCreated.last.content.length should be > 1

      passDirectoryCreated.last.filename mustBe MANIFEST_JSON_FILE_NAME
      passDirectoryCreated.last.content.length should be > 1
    }

    "create zip data with test directory" in {
      val passDirectoryCreated = fileService.createFileBytesForPass(applePass)
      val signatureContent = signatureService.createSignatureForPass(
        passDirectoryCreated, privateKey, "test", publicKey)

      val passDirectoryZipped = fileService.createPkPassZipForPass(passDirectoryCreated, signatureContent)
      passDirectoryZipped.get.nonEmpty mustBe true
      passDirectoryZipped.get.length should be > 1
    }
  }
}

object FileServiceSpec {

  implicit val passFieldFormat: OFormat[ApplePassField] = Json.format[ApplePassField]
  implicit val passGenericFormat: OFormat[ApplePassGeneric] = Json.format[ApplePassGeneric]
  implicit val passFormat: OFormat[ApplePassCard] = Json.format[ApplePassCard]

  val fileService = new FileService()
  val signatureService = new SignatureService

}