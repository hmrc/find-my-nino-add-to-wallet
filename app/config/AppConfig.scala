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

import models.admin.ApplePassCertificates2
import play.api.Configuration
import uk.gov.hmrc.mongoFeatureToggles.services.FeatureFlagService

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AppConfig @Inject() (config: Configuration, featureFlagService: FeatureFlagService)(implicit
  ec: ExecutionContext
) {

  val appName: String            = config.get[String]("appName")
  val frontendServiceUrl: String = config.get[String]("frontendServiceUrl")

  private def appleCerts: Future[(String, String, String)] =
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
  def appleWWDRCA: Future[String]                          = appleCerts.map(_._1)
  def privateCertificate: Future[String]                   = appleCerts.map(_._2)
  def privateCertificatePassword: Future[String]           = appleCerts.map(_._3)

  val googlePassIssuerId: String    = config.get[String]("googlePass.issuerId")
  val googlePassKey: String         = config.get[String]("googlePass.key")
  val googlePassExpiryYears: Int    = config.get[Int]("googlePass.expiry")
  val googlePassAddUrl: String      = config.get[String]("googlePass.url")
  val googlePassOrigins: String     = config.get[String]("googlePass.origins")
  val cacheTtl: Long                = config.get[Int]("mongodb.timeToLiveInSeconds")
  val sessionCacheTTLInSeconds: Int = config.get[Int]("mongodb.session-cache.timeToLiveInSeconds")

  val encryptionKey: String      = config.get[String]("mongodb.encryption.key")
  val encryptionEnabled: Boolean = config.get[Boolean]("mongodb.encryption.enabled")

  private lazy val individualDetailsConfig: Configuration = config.get[Configuration]("external-url.individual-details")

  lazy val individualDetailsToken: String            = individualDetailsConfig.get[String]("auth-token")
  lazy val individualDetailsEnvironment: String      = individualDetailsConfig.get[String]("environment")
  lazy val individualDetailsOriginatorId: String     = individualDetailsConfig.get[String]("originator-id")
  private lazy val individualDetailsProtocol: String = individualDetailsConfig.get[String]("protocol")
  private lazy val individualDetailsHost: String     = individualDetailsConfig.get[String]("host")
  private lazy val individualDetailsPort: String     = individualDetailsConfig.get[String]("port")
  val individualDetailsServiceUrl: String            =
    s"$individualDetailsProtocol://$individualDetailsHost:$individualDetailsPort"

  private lazy val npsCrnConfig: Configuration = config.get[Configuration]("microservice.services.nps-crn-api")

  lazy val npsCrnCorrelationIdKey: String  = npsCrnConfig.get[String]("correlationId.key")
  lazy val npsCrnOriginatorIdKey: String   = npsCrnConfig.get[String]("govUkOriginatorId.key")
  lazy val npsCrnOriginatorIdValue: String = npsCrnConfig.get[String]("govUkOriginatorId.value")

  private lazy val npsCrnProtocol: String = npsCrnConfig.get[String]("protocol")
  private lazy val npsCrnHost: String     = npsCrnConfig.get[String]("host")
  private lazy val npsCrnPort: String     = npsCrnConfig.get[String]("port")
  lazy val npsCrnToken: String            = npsCrnConfig.get[String]("token")
  val npsCrnUrl: String                   = s"$npsCrnProtocol://$npsCrnHost:$npsCrnPort"

  private lazy val fandfConfig: Configuration = config.get[Configuration]("microservice.services.fandf")

  private lazy val fandfProtocol: String = fandfConfig.get[String]("protocol")
  private lazy val fandfHost: String     = fandfConfig.get[String]("host")
  private lazy val fandfPort: String     = fandfConfig.get[String]("port")
  val fandfServiceUrl: String            = s"$fandfProtocol://$fandfHost:$fandfPort"
}
