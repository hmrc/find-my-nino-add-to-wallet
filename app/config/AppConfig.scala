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

package config

import models.admin.ApplePassCertificates2

import javax.inject.{Inject, Singleton}
import play.api.Configuration
import uk.gov.hmrc.mongoFeatureToggles.services.FeatureFlagService

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AppConfig @Inject()(config: Configuration, featureFlagService: FeatureFlagService)(implicit ec: ExecutionContext) {

  val appName: String = config.get[String]("appName")
  val frontendServiceUrl: String = config.get[String]("frontendServiceUrl")

  private def appleCerts: Future[(String, String, String)] = {
    featureFlagService.get(ApplePassCertificates2).map { featureFlag =>
      if (featureFlag.isEnabled) {
        (
          config.get[String]("applePass.appleWWDRCA2"),
          config.get[String]("applePass.privateCertificate2"),
          config.get[String]("applePass.privateCertificatePassword2")
        )
      } else {
        (
          config.get[String]("applePass.appleWWDRCA"),
          config.get[String]("applePass.privateCertificate"),
          config.get[String]("applePass.privateCertificatePassword")
        )
      }
    }
  }
  def appleWWDRCA: Future[String] = appleCerts.map(_._1)
  def privateCertificate: Future[String] = appleCerts.map(_._2)
  def privateCertificatePassword: Future[String] = appleCerts.map(_._3)

  val googleIssuerId: String = config.get[String]("googlePass.issuerId")
  val googleKey: String = config.get[String]("googlePass.key")
  val googleJWTExpiry: Int = config.get[Int]("googlePass.expiry")
  val googleAddUrl: String = config.get[String]("googlePass.url")
  val googleOrigins: String = config.get[String]("googlePass.origins")
  val cacheTtl: Int = config.get[Int]("mongodb.timeToLiveInSeconds")



  val encryptionKey: String = config.get[String]("mongodb.encryption.key")

  lazy val individualDetailsToken: String = config.get[String]("external-url.individual-details.auth-token")
  lazy val individualDetailsEnvironment: String = config.get[String]("external-url.individual-details.environment")
  lazy val individualDetailsOriginatorId: String = config.get[String]("external-url.individual-details.originator-id")
  lazy val individualDetailsProtocol: String = config.get[String]("external-url.individual-details.protocol")
  lazy val individualDetailsHost: String = config.get[String]("external-url.individual-details.host")
  lazy val individualDetailsPort: String = config.get[String]("external-url.individual-details.port")
  val individualDetailsServiceUrl: String = s"$individualDetailsProtocol://$individualDetailsHost:$individualDetailsPort"

  lazy val npsCrnCorrelationIdKey: String = config.get[String]("microservice.services.nps-crn-api.correlationId.key")
  lazy val npsCrnOriginatorIdKey: String = config.get[String]("microservice.services.nps-crn-api.govUkOriginatorId.key")
  lazy val npsCrnOriginatorIdValue: String = config.get[String]("microservice.services.nps-crn-api.govUkOriginatorId.value")

  lazy val npsCrnProtocol: String = config.get[String]("microservice.services.nps-crn-api.protocol")
  lazy val npsCrnHost: String = config.get[String]("microservice.services.nps-crn-api.host")
  lazy val npsCrnPort: String = config.get[String]("microservice.services.nps-crn-api.port")
  lazy val npsCrnToken: String = config.get[String]("microservice.services.nps-crn-api.token")
  val npsCrnUrl: String = s"$npsCrnProtocol://$npsCrnHost:$npsCrnPort"

}
