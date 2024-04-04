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

import java.io.{ByteArrayOutputStream, File}
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.util.zip.{ZipEntry, ZipOutputStream}
import javax.inject.Inject
import scala.reflect.io.Directory
import scala.util.{Success, Try}

class FileService @Inject()() extends Logging {

  import FileService._

  def createFileBytesForPass(pass: ApplePassCard): List[(String, Array[Byte])] = {

    val iconSource = getClass.getResourceAsStream(ICON_RESOURCE_PATH).readAllBytes()
    val logoSource = getClass.getResourceAsStream(LOGO_RESOURCE_PATH).readAllBytes()

    val filePass = (PASS_FILE_NAME, Json.toJson(pass).toString().getBytes(StandardCharsets.UTF_8))
    val iconFile = (ICON_FILE_NAME, iconSource)
    val logoFile = (LOGO_FILE_NAME, logoSource)

    val manifestInput = List(filePass, iconFile, logoFile)

    val createdManifest = createManifest(manifestInput).getOrElse(("", Array.emptyByteArray))

    logger.info(s"[Creating Files in Memory For Pass] isManifestCreated: $createdManifest")

    List(filePass, iconFile, logoFile, createdManifest)

  }

  def createPkPassZipForPass(passContent:  List[(String, Array[Byte])]): Option[Array[Byte]] = {
    Try {

      val byteArrayOStream = new ByteArrayOutputStream()
      val zip = new ZipOutputStream(byteArrayOStream)

      passContent.foreach { case (filename, content) =>
        zip.putNextEntry(new ZipEntry(filename))
        zip.write(content)
        zip.closeEntry()
      }
      zip.close()
      byteArrayOStream
    } match {
      case Success(value) => Some(value.toByteArray)
      case _ => None
    }
  }

  def deleteDirectory(path: Path): Boolean = {
    Try {
      val directory = new Directory(new File(path.toString))
      directory.deleteRecursively()
    } match {
      case Success(_) => true
      case _ => false
    }
  }

  private def createManifest(files: List[(String, Array[Byte])]): Option[(String, Array[Byte])] = {
    try {
      logger.info("[CREATE MANIFEST] Creating manifest")
      val map: Map[String, String] = files.map { case (filename, content) => (filename, Hashing.sha1().hashBytes(content).toString) }.toMap
      (MANIFEST_JSON_FILE_NAME, Json.toJson(map).toString().getBytes(StandardCharsets.UTF_8))
    } match {
      case (filename, content) => Some(filename, content)
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
