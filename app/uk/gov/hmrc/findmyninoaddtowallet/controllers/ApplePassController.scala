/*
 * Copyright 2022 HM Revenue & Customs
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

package uk.gov.hmrc.findmyninoaddtowallet.controllers

import play.api.Logging
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.findmyninoaddtowallet.services.ApplePassService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton()
class ApplePassController @Inject()(cc: ControllerComponents, val passService: ApplePassService)(implicit ec: ExecutionContext)
  extends BackendController(cc)
    with Logging {

  case class PassRequest(name: String, nino: String)

  implicit val passRequestFormatter = Json.format[PassRequest]

  def createPass(): Action[AnyContent] = Action.async { implicit request =>
    val passRequest = request.body.asJson.get.as[PassRequest]
    logger.debug(message = s"[Create Pass Event]$passRequest")
    Future(passService.createPass(passRequest.name, passRequest.nino) match {
      case Right(value) => Ok(value)
      case Left(exp) => InternalServerError(Json.obj(
        "status" -> "500",
        "message" -> exp.getMessage
      ))
    })
  }

  def getPassCardByPassId(passId: String): Action[AnyContent] = Action.async { implicit request =>
    logger.debug(message = s"[Get Pass Card] $passId")
    passService.getPassCardByPassId(passId).map {
      case Some(data) => Ok(data).withHeaders("Content-Disposition" -> "attachment; filename=NinoPass.pkpass")
      case _ => NotFound
    }
  }

  def getQrCodeByPassId(passId: String): Action[AnyContent] = Action.async { implicit request =>
    logger.debug(message = s"[Get QR Code] $passId")
    passService.getQrCodeByPassId(passId).map {
      case Some(data) => Ok(data).withHeaders("Content-Disposition" -> "attachment; filename=QrCode.png")
      case _ => NotFound
    }
  }
}
