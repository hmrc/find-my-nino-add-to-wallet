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

package controllers

import auth.FMNAuth
import connectors.FandFConnector
import play.api.i18n.I18nSupport
import play.api.mvc.{MessagesControllerComponents, RequestHeader}
import play.api.{Configuration, Environment}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.{Inject, Singleton}

@Singleton
class FMNBaseController @Inject() (val authConnector: AuthConnector, fandFConnector: FandFConnector)(implicit
  val config: Configuration,
  val env: Environment,
  cc: MessagesControllerComponents
) extends BackendController(cc)
    with FMNAuth(fandFConnector)
    with I18nSupport {

  override implicit def hc(implicit rh: RequestHeader): HeaderCarrier =
    HeaderCarrierConverter.fromRequest(rh)
}
