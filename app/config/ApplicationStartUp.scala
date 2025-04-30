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

package config

import models.admin._
import play.api.Logging
import uk.gov.hmrc.mongoFeatureToggles.model.FeatureFlagNamesLibrary
import util.CertificatesCheck

import java.time.Instant
import java.time.temporal.ChronoUnit.DAYS
import java.util.Date
import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class ApplicationStartUp @Inject() (certificatesCheck: CertificatesCheck)(implicit ec: ExecutionContext)
    extends Logging {
  FeatureFlagNamesLibrary.addFlags(AllFeatureFlags.list)

  certificatesCheck.getPrivateCertificateDetails.map { certs =>
    if (certs._1.before(Date.from(Instant.now().plus(60, DAYS)))) {
      logger.error(
        s"privateCertificate issued by ${certs._2} with subject ${certs._3} expires in less than 60 days on ${certs._1}"
      )
    } else {
      logger.info(s"privateCertificate issued by ${certs._2} with subject ${certs._3} expires on ${certs._1}")
    }
  }

  certificatesCheck.getAppleWWDRCADetails.map { certs =>
    if (certs._1.before(Date.from(Instant.now().plus(60, DAYS)))) {
      logger.error(
        s"Apple WWDR Intermediate Certificate issued by ${certs._2} with subject ${certs._3} expires in less than 60 days on ${certs._1}"
      )
    } else {
      logger.info(
        s"Apple WWDR Intermediate Certificate issued by ${certs._2} with subject ${certs._3} expires on ${certs._1}"
      )
    }
  }
}
