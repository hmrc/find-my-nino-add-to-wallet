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

import com.google.common.hash.Hashing
import com.google.common.io.{Files => ioFiles}
import models.apple.{ApplePassCard, ApplePassField, ApplePassGeneric}
import play.api.Logging
import play.api.libs.json.{Json, OFormat}

import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.util.zip.{ZipEntry, ZipOutputStream}
import javax.inject.Inject
import scala.util.{Success, Try}

case class FileAsBytes(filename: String, content: Array[Byte])

class FileService @Inject()() extends Logging {

  import FileService._

  def createFileBytesForPass(pass: ApplePassCard): List[FileAsBytes] = {

    val iconSource = getClass.getResourceAsStream(ICON_RESOURCE_PATH).readAllBytes()
    val logoSource = getClass.getResourceAsStream(LOGO_RESOURCE_PATH).readAllBytes()

    val filePass = FileAsBytes(PASS_FILE_NAME, Json.toJson(pass).toString().getBytes(StandardCharsets.UTF_8))
    val iconFile = FileAsBytes(ICON_FILE_NAME, iconSource)
    val logoFile = FileAsBytes(LOGO_FILE_NAME, logoSource)

    val manifestInput = List(filePass, iconFile, logoFile)
    logger.info(s"[Creating Files in Memory For Pass] isPassGenerated: ${manifestInput.nonEmpty}")
    val createdManifest: FileAsBytes = createManifest(manifestInput).getOrElse(FileAsBytes("", Array.emptyByteArray))
    logger.info(s"[Creating Files in Memory For Pass] isManifestCreated: ${createdManifest.content.nonEmpty}")

    if (filePass.content.nonEmpty && iconFile.content.nonEmpty && logoFile.content.nonEmpty && createdManifest.content.nonEmpty) {
      List(filePass, iconFile, logoFile, createdManifest)
    } else {
      List.empty
    }
  }

  def createPkPassZipForPass(passContent: List[FileAsBytes], signatureContent: FileAsBytes): Option[Array[Byte]] = {
    Try {
      val byteArrayOStream = new ByteArrayOutputStream()
      val zip = new ZipOutputStream(byteArrayOStream)

      passContent.foreach { file =>
        zip.putNextEntry(new ZipEntry(file.filename))
        zip.write(file.content)
        zip.closeEntry()
      }
      //add signature file to zip file
      zip.putNextEntry(new ZipEntry(signatureContent.filename))
      zip.write(signatureContent.content)
      zip.closeEntry()
      zip.close()
      byteArrayOStream
    } match {
      case Success(value) => Some(value.toByteArray)
      case _ => None
    }
  }

  private def createManifest(files: List[FileAsBytes]): Option[FileAsBytes] = {
    try {
      logger.info("[CREATE MANIFEST] Creating manifest")
      val map: Map[String, String] = files.map { p => (p.filename, Hashing.sha1().hashBytes(p.content).toString) }.toMap
      logger.info(s"[CREATE MANIFEST] File count: ${map.size}")
      FileAsBytes(MANIFEST_JSON_FILE_NAME, Json.toJson(map).toString().getBytes(StandardCharsets.UTF_8))
    } match {
      case FileAsBytes(filename, content) => Some(FileAsBytes(filename, content))
      case _ => None
    }
  }
}

object FileService {
  implicit val passFieldFormat: OFormat[ApplePassField] = Json.format[ApplePassField]
  implicit val passGenericFormat: OFormat[ApplePassGeneric] = Json.format[ApplePassGeneric]
  implicit val passFormat: OFormat[ApplePassCard] = Json.format[ApplePassCard]

  val PASS_FILE_NAME = "pass.json"
  val ICON_FILE_NAME = "icon.png"
  val LOGO_FILE_NAME = "logo.png" //top of the card logo
  val MANIFEST_JSON_FILE_NAME = "manifest.json"
  val ICON_RESOURCE_PATH = s"/resources/pass/$ICON_FILE_NAME"
  val LOGO_RESOURCE_PATH = s"/resources/pass/$LOGO_FILE_NAME"
}

