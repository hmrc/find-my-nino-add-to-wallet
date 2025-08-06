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

package connectors

import cats.data.EitherT
import com.google.inject.name.Named
import com.google.inject.{ImplementedBy, Inject, Singleton}
import config.AppConfig
import models.CorrelationId
import play.api.Logging
import play.api.http.Status.{BAD_REQUEST, NOT_FOUND, UNPROCESSABLE_ENTITY}
import play.api.libs.json.{Format, JsValue}
import repositories.cache.FMNSessionCacheRepository
import services.SensitiveFormatService
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps, UpstreamErrorResponse}
import uk.gov.hmrc.mongo.cache.DataKey

import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[CachingIndividualDetailsConnector])
trait IndividualDetailsConnector {
  def getIndividualDetails(nino: String, resolveMerge: String)(implicit
    ec: ExecutionContext,
    headerCarrier: HeaderCarrier
  ): EitherT[Future, UpstreamErrorResponse, JsValue]
  def deleteIndividualDetailsIfCached(nino: String)(implicit
    ec: ExecutionContext,
    headerCarrier: HeaderCarrier
  ): EitherT[Future, UpstreamErrorResponse, Unit]
}

@Singleton
class DefaultIndividualDetailsConnector @Inject() (val httpClientV2: HttpClientV2, appConfig: AppConfig)
    extends IndividualDetailsConnector
    with Logging {

  private val extraDesHeaders: Seq[(String, String)] = Seq(
    "Authorization" -> s"Bearer ${appConfig.individualDetailsToken}",
    "CorrelationId" -> CorrelationId.random.value.toString,
    "Content-Type"  -> "application/json",
    "Environment"   -> appConfig.individualDetailsEnvironment,
    "OriginatorId"  -> appConfig.individualDetailsOriginatorId
  )

  override def getIndividualDetails(nino: String, resolveMerge: String)(implicit
    ec: ExecutionContext,
    headerCarrier: HeaderCarrier
  ): EitherT[Future, UpstreamErrorResponse, JsValue] = {

    implicit val hc: HeaderCarrier = headerCarrier.withExtraHeaders(extraDesHeaders: _*)
    val url                        =
      s"${appConfig.individualDetailsServiceUrl}/individuals/details/NINO/${nino.take(8)}?resolveMerge=$resolveMerge"

    val apiResponse: Future[Either[UpstreamErrorResponse, HttpResponse]] = httpClientV2
      .get(url"$url")
      .execute[Either[UpstreamErrorResponse, HttpResponse]](readEitherOf(readRaw))
    EitherT(apiResponse).map(_.json)
  }

  override def deleteIndividualDetailsIfCached(nino: String)(implicit
    ec: ExecutionContext,
    headerCarrier: HeaderCarrier
  ): EitherT[Future, UpstreamErrorResponse, Unit] = EitherT(Future.successful(Right((): Unit)))
}

@Singleton
class CachingIndividualDetailsConnector @Inject() (
  underlying: DefaultIndividualDetailsConnector,
  sessionCacheRepository: FMNSessionCacheRepository,
  sensitiveFormatService: SensitiveFormatService
)(implicit ec: ExecutionContext)
    extends IndividualDetailsConnector
    with Logging {

  private def cache[L, A: Format](
    key: String
  )(f: => EitherT[Future, L, A])(implicit hc: HeaderCarrier): EitherT[Future, L, A] = {
    def fetchAndCache: EitherT[Future, L, A] = for {
      result <- f
      _      <- EitherT.liftF(sessionCacheRepository.putSession[A](DataKey[A](key), result))
    } yield result

    EitherT {
      sessionCacheRepository.getFromSession[A](DataKey[A](key)).flatMap {
        case Some(value) => Future.successful(Right(value))
        case None        => fetchAndCache.value
      }
    }
  }

  override def getIndividualDetails(nino: String, resolveMerge: String)(implicit
    ec: ExecutionContext,
    headerCarrier: HeaderCarrier
  ): EitherT[Future, UpstreamErrorResponse, JsValue] =
    cache(s"getIndividualDetails-$nino") {
      underlying.getIndividualDetails(nino, resolveMerge)
    }(sensitiveFormatService.sensitiveFormatFromReadsWrites[JsValue])

  override def deleteIndividualDetailsIfCached(nino: String)(implicit
    ec: ExecutionContext,
    headerCarrier: HeaderCarrier
  ): EitherT[Future, UpstreamErrorResponse, Unit] =
    EitherT.liftF(sessionCacheRepository.deleteFromSession(DataKey(s"getIndividualDetails-$nino")))

}
