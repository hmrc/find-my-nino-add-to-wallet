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

import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.{BarcodeFormat, EncodeHintType}

import java.awt.image.BufferedImage
import java.awt.{Color, Graphics2D}
import java.io.ByteArrayOutputStream
import java.util.Hashtable
import javax.imageio.ImageIO
import javax.inject.Inject
import scala.util.{Success, Try}

class QrCodeService @Inject()() {

  import QrCodeService._

  def createQRCode(qrText: String, imageSize: Int = DEFAULT_BARCODE_SIZE): Option[Array[Byte]] = {
    Try {
      val hintMap = new Hashtable[EncodeHintType, Any]
      hintMap.put(EncodeHintType.CHARACTER_SET, UTF_8)
      val qrCodeWriter = new QRCodeWriter
      val byteMatrix = qrCodeWriter.encode(qrText, BarcodeFormat.QR_CODE, imageSize, imageSize, hintMap)

      // Make the BufferedImage that are to hold the QRCode
      val matrixWidth = byteMatrix.getWidth
      val image = new BufferedImage(matrixWidth, matrixWidth, BufferedImage.TYPE_INT_RGB)
      image.createGraphics
      val graphics = image.getGraphics.asInstanceOf[Graphics2D]
      graphics.setColor(Color.WHITE)
      graphics.fillRect(0, 0, matrixWidth, matrixWidth)

      // Paint and save the image using the ByteMatrix
      graphics.setColor(Color.BLACK)
      for (i <- 0 until matrixWidth) {
        for (j <- 0 until matrixWidth) {
          if (byteMatrix.get(i, j)) graphics.fillRect(i, j, 1, 1)
        }
      }
      val byteArrayOStream = new ByteArrayOutputStream();
      ImageIO.write(image, FILE_TYPE, byteArrayOStream);
      byteArrayOStream.toByteArray
    } match {
      case Success(value) => Some(value)
      case _ => None
    }
  }
}

object QrCodeService {
  val FILE_TYPE = "png"
  val DEFAULT_BARCODE_SIZE = 150
  val UTF_8 = "utf-8"
}
