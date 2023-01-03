/*
 * Copyright 2023 HM Revenue & Customs
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

import org.mockito.MockitoSugar
import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatest.wordspec.AsyncWordSpec

class QrCodeServiceSpec extends AsyncWordSpec with Matchers with MockitoSugar {

  import QrCodeServiceSpec._

  "createQRCode" must {
    "return the QR Code Data for given qrText" in {
      val qrCodeText = "https://test.gov.uk/test-url/30b90407-142f-43cd-bfe2-ed90d75de1c6"

      val optionQrCode = qrCodeService.createQRCode(qrCodeText)
      optionQrCode.isDefined mustBe true
      optionQrCode.get.length should be > qrCodeText.length
    }
  }
}

object QrCodeServiceSpec {
  val qrCodeService = new QrCodeService()
}
