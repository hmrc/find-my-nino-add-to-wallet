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

import models.apple.ApplePassCard
import org.mockito.MockitoSugar
import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatest.wordspec.AsyncWordSpec

import java.nio.file.Paths
import java.util.UUID
import scala.reflect.io.File

class FileServiceSpec extends AsyncWordSpec with Matchers with MockitoSugar {
  import services.FileServiceSpec._

  "fileService" must {
    "should create test.pass directory with icon, thumbnail, pass.json, manifest files" in {
      val uuid = UUID.randomUUID().toString
      val DEFAULT_EXPIRATION_YEARS = 100
      val applePass = ApplePassCard(
        "Test Pass", "AB 12 34 56 Q",
        uuid)
      val passDirectoryCreated = fileService.createFileBytesForPass(applePass)
      passDirectoryCreated mustBe true
      File("./test.pass").exists mustBe true
      File("./test.pass/icon.png").exists mustBe true
      File("./test.pass/manifest.json").exists mustBe true
      File("./test.pass/pass.json").exists mustBe true
    }

    "should create zip data with test directory" in {
      val passDirectoryZipped = fileService.createPkPassZipForPass(Paths.get("./test.pass"))
      passDirectoryZipped.isDefined mustBe true
      passDirectoryZipped.get.length should be > 1
    }

    "should delete test.pass directory" in {
      val passDirectoryDeleted = fileService.deleteDirectory(Paths.get("./test.pass"))
      passDirectoryDeleted mustBe true
      File("./test.pass").exists mustBe false
    }
  }
}

object FileServiceSpec {
  val fileService = new FileService()
}