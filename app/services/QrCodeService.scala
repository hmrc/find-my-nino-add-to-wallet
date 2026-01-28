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

import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.{BarcodeFormat, EncodeHintType}

import java.awt.Color
import java.io.ByteArrayOutputStream
import java.util.Hashtable
import javax.imageio.ImageIO
import javax.inject.Inject
import scala.util.{Success, Try}

class QrCodeService @Inject() () {

  import QrCodeService.*

  def createQRCode(qrText: String, imageSize: Int = DEFAULT_BARCODE_SIZE): Option[Array[Byte]] =
    Try {
      val hintMap = new Hashtable[EncodeHintType, Any]
      hintMap.put(EncodeHintType.CHARACTER_SET, UTF_8)
      hintMap.put(EncodeHintType.MARGIN, 4)

      val byteMatrix = new QRCodeWriter().encode(qrText, BarcodeFormat.QR_CODE, imageSize, imageSize, hintMap)

      val matrixWidth = byteMatrix.getWidth
      val image       = new java.awt.image.BufferedImage(matrixWidth, matrixWidth, java.awt.image.BufferedImage.TYPE_INT_RGB)
      val graphics    = image.getGraphics.asInstanceOf[java.awt.Graphics2D]

      graphics.setColor(Color.WHITE)
      graphics.fillRect(0, 0, matrixWidth, matrixWidth)

      graphics.setColor(Color.BLACK)
      for (i <- 0 until matrixWidth)
        for (j <- 0 until matrixWidth)
          if (byteMatrix.get(i, j)) graphics.fillRect(i, j, 1, 1)

      val out = new ByteArrayOutputStream()
      ImageIO.write(image, FILE_TYPE, out)
      graphics.dispose()

      out.toByteArray
    } match {
      case Success(value) => Some(value)
      case _              => None
    }
}

object QrCodeService {
  val FILE_TYPE            = "png"
  val DEFAULT_BARCODE_SIZE = 200
  val UTF_8                = "utf-8"
}
