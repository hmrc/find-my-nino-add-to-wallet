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

import com.google.inject.{ImplementedBy, Inject, Singleton}
import config.AppConfig
import models.CorrelationId
import play.api.Logging
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}

import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[DefaultIndividualDetailsConnector])
trait IndividualDetailsConnector {
  def getIndividualDetails(nino: String, resolveMerge: String)(implicit
    ec: ExecutionContext,
    headerCarrier: HeaderCarrier
  ): Future[HttpResponse]
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
  ): Future[HttpResponse] = {

    implicit val hc: HeaderCarrier = headerCarrier.withExtraHeaders(extraDesHeaders: _*)
    val url                        =
      s"${appConfig.individualDetailsServiceUrl}/individuals/details/NINO/${nino.take(8)}?resolveMerge=$resolveMerge"

    httpClientV2
      .get(url"$url")
      .execute[HttpResponse]
  }
}

//@Singleton
//class CachingIndividualDetailsConnector @Inject() (
//                                                 @Named("default") underlying: IndividualDetailsConnector,
//                                                 sessionCacheRepository: PertaxSessionCacheRepository,
//                                                 sensitiveFormatService: SensitiveFormatService
//                                               )(implicit ec: ExecutionContext)
//  extends IndividualDetailsConnector
//    with Logging {
//
//  private def cache[L, A: Format](
//                                   key: String
//                                 )(f: => EitherT[Future, L, A])(implicit hc: HeaderCarrier): EitherT[Future, L, A] = {
//    def fetchAndCache: EitherT[Future, L, A] = for {
//      result <- f
//      _      <- EitherT.liftF(sessionCacheRepository.putSession[A](DataKey[A](key), result))
//    } yield result
//
//    EitherT {
//      sessionCacheRepository.getFromSession[A](DataKey[A](key)).flatMap {
//        case Some(value) => Future.successful(Right(value))
//        case None        => fetchAndCache.value
//      }
//    }
//  }
//
//  override def getIndividualDetails(nino: String, resolveMerge: String)(implicit
//                                                                                     ec: ExecutionContext,
//                                                                                     headerCarrier: HeaderCarrier
//  ): Future[HttpResponse] =
//    cache(s"getIndividualDetails-$nino") {
//      underlying.getIndividualDetails(nino, resolveMerge)
//    }(sensitiveFormatService.sensitiveFormatFromReadsWrites[JsValue])
//}
