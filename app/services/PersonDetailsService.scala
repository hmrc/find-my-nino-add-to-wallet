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

import config.AppConfig
import play.api.Logging
import repositories.PersonDetailsRepo

import java.util.UUID
import javax.inject._
import scala.concurrent.{ExecutionContext, Future}

class PersonDetailsService @Inject()(val config: AppConfig,
                                 val personDetailsRepo: PersonDetailsRepo,
                                 ) extends Logging {

  def getPersonDetailsById(pdId: String)(implicit ec: ExecutionContext): Future[Option[String]] = {
    personDetailsRepo.findById(pdId).map(_.map(_.personDetails))
  }

  def createPersonDetails(fullName: String, nino: String, personDetails:String, expirationDate: String)
                         (implicit ec: ExecutionContext): Either[Exception, String] = {
    val uuid = UUID.randomUUID().toString
    personDetailsRepo.insert(uuid, fullName, nino, personDetails, expirationDate )
      Right(uuid)

  }
}

