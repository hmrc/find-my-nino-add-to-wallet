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

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.must.Matchers.must
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.libs.json.*
import services.SensitiveFormatService.SensitiveJsValue
import uk.gov.hmrc.crypto.{Crypted, Decrypter, Encrypter, PlainText}
import _root_.util.SpecBase

class SensitiveFormatServiceSpec extends SpecBase with BeforeAndAfterEach {
  private trait EncrypterDecrypter extends Encrypter with Decrypter

  private implicit val mockEncrypterDecrypter: EncrypterDecrypter = mock[EncrypterDecrypter]
  private val encryptedValueAsString: String                      = "encrypted"
  private val encryptedValue: Crypted                             = Crypted(encryptedValueAsString)
  private val unencryptedJsObject: JsObject                       = Json.obj(
    "testa" -> "valuea",
    "testb" -> "valueb"
  )
  private val unencryptedJsString: JsString                       = JsString("test")
  private val sensitiveJsObject: SensitiveJsValue                 = SensitiveJsValue(unencryptedJsObject)
  private val sensitiveJsString: SensitiveJsValue                 = SensitiveJsValue(unencryptedJsString)

  private val sensitiveFormatService = new SensitiveFormatService(mockEncrypterDecrypter)

  val fakePersonDetails: String =
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
    reset(mockEncrypterDecrypter)
  }

  "sensitiveFormatJsValue" must {
    "write JsObject, calling encrypt" in {
      when(mockEncrypterDecrypter.encrypt(any())).thenReturn(encryptedValue)

      val result: JsValue = Json.toJson(sensitiveJsObject)(sensitiveFormatService.sensitiveFormatJsValue[JsObject])

      result mustBe JsString(encryptedValueAsString)

      verify(mockEncrypterDecrypter, times(1)).encrypt(any())
    }

    "write JsString, calling encrypt" in {
      when(mockEncrypterDecrypter.encrypt(any())).thenReturn(encryptedValue)

      val result: JsValue = Json.toJson(sensitiveJsString)(sensitiveFormatService.sensitiveFormatJsValue[JsString])

      result mustBe JsString(encryptedValueAsString)

      verify(mockEncrypterDecrypter, times(1)).encrypt(any())
    }

    "read JsString as a JsObject, calling decrypt" in {
      when(mockEncrypterDecrypter.decrypt(any())).thenReturn(PlainText(Json.stringify(unencryptedJsObject)))

      val result =
        JsString(encryptedValueAsString).as[SensitiveJsValue](sensitiveFormatService.sensitiveFormatJsValue[JsObject])

      result mustBe sensitiveJsObject

      verify(mockEncrypterDecrypter, times(1)).decrypt(any())
    }

    "decrypt an encrypted JsString and return the original JsString" in {
      when(mockEncrypterDecrypter.decrypt(any())).thenReturn(PlainText(Json.stringify(JsString("test"))))

      val result =
        JsString(encryptedValueAsString).as[SensitiveJsValue](sensitiveFormatService.sensitiveFormatJsValue[JsString])

      result mustBe SensitiveJsValue(JsString("test"))

      verify(mockEncrypterDecrypter, times(1)).decrypt(any())
    }

    "read JsString as a JsString, calling decrypt unsuccessfully (i.e. not encrypted) and use unencrypted jsString" in {
      when(mockEncrypterDecrypter.decrypt(any())).thenThrow(new SecurityException("Unable to decrypt value"))

      val result = JsString("abc").as[SensitiveJsValue](sensitiveFormatService.sensitiveFormatJsValue[JsString])

      result mustBe SensitiveJsValue(JsString("abc"))

      verify(mockEncrypterDecrypter, times(1)).decrypt(any())
    }

    "read JsObject, not calling decrypt at all" in {
      val result = unencryptedJsObject.as[SensitiveJsValue](sensitiveFormatService.sensitiveFormatJsValue[JsObject])

      result mustBe sensitiveJsObject

      verify(mockEncrypterDecrypter, times(0)).decrypt(any())
    }
  }

  "sensitiveFormatFromReadsWrites" must {
    "write encrypted array, calling encrypt" in {
      when(mockEncrypterDecrypter.encrypt(any())).thenReturn(encryptedValue)
      val result: JsValue =
        Json
          .toJson(Json.parse(fakePersonDetails))(sensitiveFormatService.sensitiveFormatFromReadsWrites[JsValue])

      result mustBe JsString(encryptedValueAsString)

      verify(mockEncrypterDecrypter, times(1)).encrypt(any())
    }

    "read encrypted value, calling decrypt" in {
      when(mockEncrypterDecrypter.decrypt(any()))
        .thenReturn(PlainText(fakePersonDetails))

      val result = JsString(encryptedValueAsString).as[JsValue](
        sensitiveFormatService.sensitiveFormatFromReadsWrites[JsValue]
      )

      result mustBe Json.parse(fakePersonDetails)

      verify(mockEncrypterDecrypter, times(1)).decrypt(any())
    }

    "read unencrypted JsObject, not calling decrypt at all" in {
      val result =
        Json.parse(fakePersonDetails).as[JsValue](sensitiveFormatService.sensitiveFormatFromReadsWrites[JsValue])

      result mustBe Json.parse(fakePersonDetails)

      verify(mockEncrypterDecrypter, times(0)).decrypt(any())

    }
  }

}
