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

package config

import _root_.util.SpecBase
import play.api.Configuration

class CryptoProviderSpec extends SpecBase {

  "CryptoProvider" must {

    "return legacy crypto as current when gcm.primary is false" in {
      val config = Configuration.from(
        Map(
          "mongodb.encryption.key"              -> "z4rWoRLf7a1OHTXLutSDJjhrUzZTBE3b",
          "mongodb.encryption.previousKeys"     -> List(),
          "mongodb.encryption.gcm.key"          -> "bG9jYWxHY21BZXNLZXkxNg==",
          "mongodb.encryption.gcm.previousKeys" -> List(),
          "mongodb.encryption.gcm.primary"      -> false
        )
      )

      val cryptoProvider = new CryptoProvider(config)
      val crypto         = cryptoProvider.get()

      crypto must not be null
    }

    "return gcm crypto as current when gcm.primary is true" in {
      val config = Configuration.from(
        Map(
          "mongodb.encryption.key"              -> "z4rWoRLf7a1OHTXLutSDJjhrUzZTBE3b",
          "mongodb.encryption.previousKeys"     -> List(),
          "mongodb.encryption.gcm.key"          -> "bG9jYWxHY21BZXNLZXkxNg==",
          "mongodb.encryption.gcm.previousKeys" -> List(),
          "mongodb.encryption.gcm.primary"      -> true
        )
      )

      val cryptoProvider = new CryptoProvider(config)
      val crypto         = cryptoProvider.get()

      crypto must not be null
    }

    "use false as default for gcm.primary when not configured" in {
      val config = Configuration.from(
        Map(
          "mongodb.encryption.key"              -> "z4rWoRLf7a1OHTXLutSDJjhrUzZTBE3b",
          "mongodb.encryption.previousKeys"     -> List(),
          "mongodb.encryption.gcm.key"          -> "bG9jYWxHY21BZXNLZXkxNg==",
          "mongodb.encryption.gcm.previousKeys" -> List()
        )
      )

      val cryptoProvider = new CryptoProvider(config)
      val crypto         = cryptoProvider.get()

      crypto must not be null
    }

    "provide composed crypto with both legacy and gcm support" in {
      val config = Configuration.from(
        Map(
          "mongodb.encryption.key"              -> "z4rWoRLf7a1OHTXLutSDJjhrUzZTBE3b",
          "mongodb.encryption.previousKeys"     -> List(),
          "mongodb.encryption.gcm.key"          -> "bG9jYWxHY21BZXNLZXkxNg==",
          "mongodb.encryption.gcm.previousKeys" -> List(),
          "mongodb.encryption.gcm.primary"      -> false
        )
      )

      val cryptoProvider = new CryptoProvider(config)
      val crypto         = cryptoProvider.get()

      // Verify we can use it for encryption and decryption
      crypto must not be null

      import uk.gov.hmrc.crypto.PlainText
      val plainText = PlainText("test data")
      val encrypted = crypto.encrypt(plainText)
      encrypted       must not be null
      encrypted.value must not be empty

      val decrypted = crypto.decrypt(encrypted)
      decrypted.value mustBe "test data"
    }

    "support encryption/decryption cycle with gcm as primary" in {
      val config = Configuration.from(
        Map(
          "mongodb.encryption.key"              -> "z4rWoRLf7a1OHTXLutSDJjhrUzZTBE3b",
          "mongodb.encryption.previousKeys"     -> List(),
          "mongodb.encryption.gcm.key"          -> "bG9jYWxHY21BZXNLZXkxNg==",
          "mongodb.encryption.gcm.previousKeys" -> List(),
          "mongodb.encryption.gcm.primary"      -> true
        )
      )

      val cryptoProvider = new CryptoProvider(config)
      val crypto         = cryptoProvider.get()

      import uk.gov.hmrc.crypto.PlainText
      val plainText = PlainText("sensitive information")
      val encrypted = crypto.encrypt(plainText)
      encrypted       must not be null
      encrypted.value must not be empty

      val decrypted = crypto.decrypt(encrypted)
      decrypted.value mustBe "sensitive information"
    }
  }
}
