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
import models.{ApplePassCard, ApplePassField, ApplePassGeneric}
import play.api.Logging
import play.api.libs.json.{JsObject, JsString, JsValue, Json, OFormat}

import java.io.{BufferedInputStream, ByteArrayOutputStream, File, FileInputStream}
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path, Paths}
import java.security.MessageDigest
import java.util.zip.{ZipEntry, ZipOutputStream}
import javax.inject.Inject
import javax.xml.bind.DatatypeConverter
import scala.reflect.io.Directory
import scala.util.{Failure, Success, Try}

class FileService @Inject()() extends Logging {

  import FileService._

  def createDirectoryForPass(path: Path, pass: ApplePassCard, enPath: Path, cyPath: Path): Boolean = {
    // Create Pass Directory:
    val isDirectoryCreated = createDirectory(path)

    val isENDirectoryCreated = createDirectory(enPath)
    val isCYDirectoryCreated = createDirectory(cyPath)

    // Write pass.json, icon and thumbnail to that directory
    val isFilePassCreated = writeToAFile(path.resolve(PASS_FILE_NAME), Json.toJson(pass).toString().getBytes(StandardCharsets.UTF_8))
    val iconSource = getClass.getResourceAsStream(ICON_RESOURCE_PATH).readAllBytes()
    val isIconFileCreated = writeToAFile(path.resolve(ICON_FILE_NAME), iconSource)
    val logoSource = getClass.getResourceAsStream(LOGO_RESOURCE_PATH).readAllBytes()
    val isLogoFileCreated = writeToAFile(path.resolve(LOGO_FILE_NAME), logoSource)
//    val thumbnailSource = getClass.getResourceAsStream(THUMBNAIL_RESOURCE_PATH).readAllBytes()
//    val isThumbnailCreated = writeToAFile(path.resolve(THUMBNAIL_FILE_NAME), thumbnailSource)
    logger.info(s"[Creating Directory For Pass] isFilePassCreated: $isFilePassCreated || " +
      s"isIconFileCreated: $isIconFileCreated || " +
      s"isLogoCreated: $isLogoFileCreated" // +
//    s"isThumbnailCreated: $isThumbnailCreated || "
    )

    //Write pass.strings to en and cy
    val enSource = getClass.getResourceAsStream(EN_PASS_STRINGS_PATH).readAllBytes()
    val enIsPassFileCreated = writeToAFile(enPath.resolve(s"$PASS_STRINGS_NAME"), enSource)
    val cySource = getClass.getResourceAsStream(CY_PASS_STRINGS_PATH).readAllBytes()
    val cyIsPassFileCreated = writeToAFile(cyPath.resolve(s"$PASS_STRINGS_NAME"), cySource)
    logger.info(s"[Creating Directory For Pass] enIsPassFileCreated: $enIsPassFileCreated | cyIsPassFileCreated: $cyIsPassFileCreated")

    // Create Manifest File:
    val isManifestCreated = createManifestFile(path)
    logger.info(s"[Creating Directory For Pass] isManifestCreated: $isManifestCreated")
    println(scala.io.Source.fromFile(s"$path/manifest.json").mkString)
    println(scala.io.Source.fromFile(s"$path/pass.json").mkString)

    Files.list(path).forEach(println)
    Files.list(enPath).forEach(println)
    Files.list(cyPath).forEach(println)

    isDirectoryCreated && isENDirectoryCreated && isCYDirectoryCreated && isFilePassCreated && isIconFileCreated && isManifestCreated && isLogoFileCreated && enIsPassFileCreated && cyIsPassFileCreated// && isThumbnailCreated
  }

//  def createPkPassZipForPass(path: Path): Option[Array[Byte]] = {
//    Try {
//      val files = path.toFile.listFiles(f => f.getName != ".DS_Store")
//      val byteArrayOStream = new ByteArrayOutputStream();
//      val zip = new ZipOutputStream(byteArrayOStream)
//
//      files.foreach { file =>
//        zip.putNextEntry(new ZipEntry(file.getName))
//        val in = new BufferedInputStream(new FileInputStream(file))
//        var byteRead = in.read()
//        while (byteRead > -1) {
//          zip.write(byteRead)
//          byteRead = in.read()
//        }
//        in.close()
//        zip.closeEntry()
//      }
//      zip.close()
//      byteArrayOStream
//    } match {
//      case Success(value) => Some(value.toByteArray)
//      case _ => None
//    }
//  }

  def createPkPassZipForPass(path: Path): Option[Array[Byte]] = {
    def addFileToZip(file: Path, zip: ZipOutputStream, parentDir: String): Unit = {
      val filePath = if (parentDir.isEmpty) file.getFileName.toString else s"$parentDir/${file.getFileName}"

      if (Files.isDirectory(file)) {
        val files = Files.list(file)
        files.forEach(f => addFileToZip(f, zip, filePath))
      } else {
        zip.putNextEntry(new ZipEntry(filePath))
        val in = new BufferedInputStream(Files.newInputStream(file))
        var byteRead = in.read()
        while (byteRead > -1) {
          zip.write(byteRead)
          byteRead = in.read()
        }
        in.close()
        zip.closeEntry()
      }
    }

    Try {
      val byteArrayOStream = new ByteArrayOutputStream()
      val zip = new ZipOutputStream(byteArrayOStream)

      val files = Files.list(path)
      files.forEach(file => addFileToZip(file, zip, ""))

      zip.close()
      byteArrayOStream
    } match {
      case Success(value) => Some(value.toByteArray)
      case Failure(ex) =>
        logger.info("createPkPassZipForPass failed", ex)
        None
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

  private def createDirectory(path: Path): Boolean = {
    Try(Files.createDirectories(path)) match {
      case Success(_) => true
      case _ => false
    }
  }

  private def writeToAFile(path: Path, bytes: Array[Byte]): Boolean = {
    Try(Files.write(path, bytes)) match {
      case Success(_) => true
      case _ => false
    }
  }

//  private def createManifestFile(path: Path): Boolean = {
//    logger.info(s"[CREATE MANIFEST FILE] Path: $path")
//    val files = path.toFile.listFiles(f => f.getName != ".DS_STORE")
//    logger.info(s"[CREATE MANIFEST FILE] File count: ${files.length}")
//    val map = files.map(f => (f.getName, ioFiles.asByteSource(f).hash(Hashing.sha1()).toString)).toMap
//    Try(Files.write(path.resolve(MANIFEST_JSON_FILE_NAME), Json.toJson(map).toString().getBytes(StandardCharsets.UTF_8))) match {
//      case Success(_) => true
//      case _ => false
//    }
//  }

  private def createManifestFile(path: Path): Boolean = {
    logger.info(s"[CREATE MANIFEST FILE] Path: $path")

    def listFiles(directoryPath: Path): Seq[Path] = {
      Files.walk(directoryPath)
        .filter(Files.isRegularFile(_))
        .toArray
        .map(_.asInstanceOf[Path])
    }

    def createJson(fileList: Seq[Path]): JsObject = {
      val jsonObjects = fileList.map { filePath =>
        val fileName = path.relativize(filePath).toString.replaceAll("/", "\\/") // Convert to relative path
        println(s"this is the filename $fileName")
        val hashValue = calculateSHA1(filePath)
        fileName -> JsString(hashValue)
      }
      JsObject(jsonObjects)
    }

    def calculateSHA1(filePath: Path): String = {
      val bytes = Files.readAllBytes(filePath)
      val sha1 = MessageDigest.getInstance("SHA-1").digest(bytes)
      DatatypeConverter.printHexBinary(sha1).toLowerCase
    }

    def writeJsonToFile(json: JsValue): Boolean = {
      val jsonString = Json.prettyPrint(json)
      Try(Files.write(path.resolve(MANIFEST_JSON_FILE_NAME), jsonString.getBytes(StandardCharsets.UTF_8))) match {
        case Success(_) => true
        case _ => false
      }
    }

    val fileList = listFiles(path)
    logger.info(s"[CREATE MANIFEST FILE] File count: ${fileList.length}")
    val json = createJson(fileList)

    writeJsonToFile(json)
  }
}

object FileService {
  implicit val passFieldFormat: OFormat[ApplePassField] = Json.format[ApplePassField]
  implicit val passGenericFormat: OFormat[ApplePassGeneric] = Json.format[ApplePassGeneric]
  implicit val passFormat: OFormat[ApplePassCard] = Json.format[ApplePassCard]

  val PASS_FILE_NAME = "pass.json"
  val ICON_FILE_NAME = "icon.png"
  val LOGO_FILE_NAME = "logo.png" //top of the card logo
  val PASS_STRINGS_NAME = "pass.strings" //top of the card logo
  val THUMBNAIL_FILE_NAME = "thumbnail.png"
  val MANIFEST_JSON_FILE_NAME = "manifest.json"
  val ICON_RESOURCE_PATH = s"/resources/pass/$ICON_FILE_NAME"
  val LOGO_RESOURCE_PATH = s"/resources/pass/$LOGO_FILE_NAME"
  val EN_PASS_STRINGS_PATH = s"/resources/pass/en/$PASS_STRINGS_NAME"
  val CY_PASS_STRINGS_PATH = s"/resources/pass/cy/$PASS_STRINGS_NAME"
  val THUMBNAIL_RESOURCE_PATH = s"/resources/pass/$THUMBNAIL_FILE_NAME"
}
