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

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, ObjectInputStream, ObjectOutputStream}
import java.util.Base64

object DeSerializer {
  def deserializeObjectFromBase64[T](base64String: String): T = {
    val decodedBytes = Base64.getDecoder.decode(base64String)
    val inputStream = new ByteArrayInputStream(decodedBytes)
    val objectInputStream = new ObjectInputStream(inputStream)
    val obj = objectInputStream.readObject()
    obj.asInstanceOf[T]
  }
}
