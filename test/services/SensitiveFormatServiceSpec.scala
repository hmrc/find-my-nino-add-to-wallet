/*
 * Copyright 2025 HM Revenue & Customs
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

import _root_.util.SpecBase
import config.{AppConfig, CryptoProvider}
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import play.api.Configuration
import play.api.libs.json.*
import services.SensitiveFormatService.SensitiveJsValue

class SensitiveFormatServiceSpec extends SpecBase with BeforeAndAfterEach {

  private val mockAppConfig: AppConfig = mock[AppConfig]

  private lazy val cryptoProvider: CryptoProvider = {
    val config = Configuration.from(
      Map(
        "mongodb.encryption.key"              -> "z4rWoRLf7a1OHTXLutSDJjhrUzZTBE3b",
        "mongodb.encryption.previousKeys"     -> List(),
        "mongodb.encryption.gcm.key"          -> "bG9jYWxHY21BZXNLZXkxNg==",
        "mongodb.encryption.gcm.previousKeys" -> List(),
        "mongodb.encryption.gcm.primary"      -> false
      )
    )
    new CryptoProvider(config)
  }

  private lazy val cryptoProviderWithGcmPrimary: CryptoProvider = {
    val config = Configuration.from(
      Map(
        "mongodb.encryption.key"              -> "z4rWoRLf7a1OHTXLutSDJjhrUzZTBE3b",
        "mongodb.encryption.previousKeys"     -> List(),
        "mongodb.encryption.gcm.key"          -> "bG9jYWxHY21BZXNLZXkxNg==",
        "mongodb.encryption.gcm.previousKeys" -> List(),
        "mongodb.encryption.gcm.primary"      -> true
      )
    )
    new CryptoProvider(config)
  }

  private lazy val sensitiveFormatServiceWithGcmPrimary =
    new SensitiveFormatService(cryptoProviderWithGcmPrimary, mockAppConfig)

  private lazy val sensitiveFormatService = new SensitiveFormatService(cryptoProvider, mockAppConfig)

  private val unencryptedJsObject: JsObject       = Json.obj(
    "testa" -> "valuea",
    "testb" -> "valueb"
  )
  private val unencryptedJsString: JsString       = JsString("test")
  private val sensitiveJsObject: SensitiveJsValue = SensitiveJsValue(unencryptedJsObject)
  private val sensitiveJsString: SensitiveJsValue = SensitiveJsValue(unencryptedJsString)

  private val fakeJsonPayload: String =
    """
      |{
      |  "person": {
      |    "firstName": "John",
      |    "lastName": "Doe",
      |    "initials": "JD",
      |    "title": "Mr",
      |    "sex": "M",
      |    "dateOfBirth": "1975-12-03",
      |    "nino": "AS664747B"
      |  },
      |  "address": {
      |    "line1": "1 Fake Street",
      |    "line2": "Fake Town",
      |    "line3": "Fake City",
      |    "line4": "Fake Region",
      |    "postcode": "AA1 1AA",
      |    "startDate": "2015-03-15",
      |    "type": "Residential",
      |    "status": 1
      |  }
      |}
      |""".stripMargin

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockAppConfig)
    when(mockAppConfig.encryptionEnabled).thenReturn(true)
    ()
  }

  "sensitiveFormatJsValue" must {

    "write JsObject with encryption" in {
      val result: JsValue = Json.toJson(sensitiveJsObject)(sensitiveFormatService.sensitiveFormatJsValue[JsObject])

      result mustNot equal(JsNull)
      result.isInstanceOf[JsString] mustBe true
    }

    "write JsString with encryption" in {
      val result: JsValue = Json.toJson(sensitiveJsString)(sensitiveFormatService.sensitiveFormatJsValue[JsString])

      result mustNot equal(JsNull)
      result.isInstanceOf[JsString] mustBe true
    }

    "read encrypted JsString as JsObject" in {
      val encrypted = Json.toJson(sensitiveJsObject)(sensitiveFormatService.sensitiveFormatJsValue[JsObject])
      val result    =
        encrypted.as[SensitiveJsValue](sensitiveFormatService.sensitiveFormatJsValue[JsObject])

      result mustBe sensitiveJsObject
    }

    "decrypt an encrypted JsString and return the original" in {
      val encrypted = Json.toJson(sensitiveJsString)(sensitiveFormatService.sensitiveFormatJsValue[JsString])
      val result    =
        encrypted.as[SensitiveJsValue](sensitiveFormatService.sensitiveFormatJsValue[JsString])

      result mustBe sensitiveJsString
    }

    "handle unencrypted value" in {
      val result = unencryptedJsObject.as[SensitiveJsValue](sensitiveFormatService.sensitiveFormatJsValue[JsObject])

      result mustBe sensitiveJsObject
    }

    "read unencrypted JsObject without decryption" in {
      val result = unencryptedJsObject.as[SensitiveJsValue](sensitiveFormatService.sensitiveFormatJsValue[JsObject])

      result mustBe sensitiveJsObject
    }
  }

  "sensitiveFormatFromReadsWrites" must {

    "write value with encryption enabled" in {
      val result: JsValue =
        Json.toJson(Json.parse(fakeJsonPayload))(sensitiveFormatService.sensitiveFormatFromReadsWrites[JsValue])

      result mustNot equal(JsNull)
      result.isInstanceOf[JsString] mustBe true
    }

    "write value without encryption when disabled" in {
      when(mockAppConfig.encryptionEnabled).thenReturn(false)

      val result: JsValue =
        Json.toJson(Json.parse(fakeJsonPayload))(sensitiveFormatService.sensitiveFormatFromReadsWrites[JsValue])

      result mustBe Json.parse(fakeJsonPayload)
    }

    "read encrypted value with decryption" in {
      val encrypted =
        Json.toJson(Json.parse(fakeJsonPayload))(sensitiveFormatService.sensitiveFormatFromReadsWrites[JsValue])
      val result    = encrypted.as[JsValue](
        sensitiveFormatService.sensitiveFormatFromReadsWrites[JsValue]
      )

      result mustBe Json.parse(fakeJsonPayload)
    }

    "read encrypted value without decryption when disabled" in {
      when(mockAppConfig.encryptionEnabled).thenReturn(false)

      val plainText = Json.parse(fakeJsonPayload)
      val result    = plainText.as[JsValue](
        sensitiveFormatService.sensitiveFormatFromReadsWrites[JsValue]
      )

      result mustBe plainText
    }

    "read unencrypted object without decryption" in {
      val result =
        Json.parse(fakeJsonPayload).as[JsValue](sensitiveFormatService.sensitiveFormatFromReadsWrites[JsValue])

      result mustBe Json.parse(fakeJsonPayload)
    }
  }

  "with gcm.primary enabled" must {

    "write JsObject with GCM encryption" in {
      val result: JsValue =
        Json.toJson(sensitiveJsObject)(sensitiveFormatServiceWithGcmPrimary.sensitiveFormatJsValue[JsObject])

      result mustNot equal(JsNull)
      result.isInstanceOf[JsString] mustBe true
    }

    "write value with GCM encryption enabled" in {
      val result: JsValue =
        Json.toJson(Json.parse(fakeJsonPayload))(
          sensitiveFormatServiceWithGcmPrimary.sensitiveFormatFromReadsWrites[JsValue]
        )

      result mustNot equal(JsNull)
      result.isInstanceOf[JsString] mustBe true
    }

    "read encrypted value with GCM decryption" in {
      val encrypted =
        Json.toJson(Json.parse(fakeJsonPayload))(
          sensitiveFormatServiceWithGcmPrimary.sensitiveFormatFromReadsWrites[JsValue]
        )
      val result    = encrypted.as[JsValue](
        sensitiveFormatServiceWithGcmPrimary.sensitiveFormatFromReadsWrites[JsValue]
      )

      result mustBe Json.parse(fakeJsonPayload)
    }

    "decrypt encrypted JsObject with GCM and return the original" in {
      val encrypted =
        Json.toJson(sensitiveJsObject)(sensitiveFormatServiceWithGcmPrimary.sensitiveFormatJsValue[JsObject])
      val result    =
        encrypted.as[SensitiveJsValue](sensitiveFormatServiceWithGcmPrimary.sensitiveFormatJsValue[JsObject])

      result mustBe sensitiveJsObject
    }
  }

}
