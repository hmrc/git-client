/*
 * Copyright 2016 HM Revenue & Customs
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

package uk.gov.hmrc.gitclient

import java.util.concurrent.{Executors, ExecutorService}

import com.ning.http.client.AsyncHttpClientConfig.Builder
import play.Logger
import play.api.libs.json.{JsValue, Reads}
import play.api.libs.ws.{WSAuthScheme, WSRequestHolder, WSResponse, DefaultWSClientConfig}
import play.api.libs.ws.ning.{NingWSClient, NingAsyncHttpClientConfigBuilder}

import scala.concurrent.{Future, ExecutionContext}
import scala.util.{Failure, Success, Try}

class HttpClient(user: String, apiKey: String) {

  private val asyncBuilder: Builder = new Builder()
  private val tp: ExecutorService = Executors.newCachedThreadPool()
  asyncBuilder.setExecutorService(tp)

  private val builder: NingAsyncHttpClientConfigBuilder =
    new NingAsyncHttpClientConfigBuilder(
      config = new DefaultWSClientConfig(/*connectionTimeout = Some(120 * 1000)*/),
      builder = asyncBuilder)

  private val ws = new NingWSClient(builder.build())

  def close() = ws.close()

  def get[T](url: String)(implicit ec: ExecutionContext, r: Reads[T]): Future[T] = withErrorHandling("GET", url) {
    case _@s if s.status >= 200 && s.status < 300 =>
      Try {
        s.json.as[T]
      } match {
        case Success(a) => a
        case Failure(e) =>
          Logger.error(s"Error paring response failed body was: ${s.body} root url : $url")
          throw e
      }
    case res =>
      throw new RuntimeException(s"Unexpected response status : ${res.status}  calling url : $url response body : ${res.body}")
  }


  private def withErrorHandling[T](method: String, url: String)(f: WSResponse => T)(implicit ec: ExecutionContext): Future[T] = {
    buildCall(method, url).execute().transform(
      f,
      _ => throw new RuntimeException(s"Error connecting  $url")
    )
  }

  private def buildCall(method: String, url: String, body: Option[JsValue] = None): WSRequestHolder = {
    val req = ws.url(url)
      .withMethod(method)
      .withAuth(user, apiKey, WSAuthScheme.BASIC)
      .withQueryString("client_id" -> user, "client_secret" -> apiKey)
      .withHeaders("content-type" -> "application/json")

    body.map { b =>
      req.withBody(b)
    }.getOrElse(req)
  }

  def head(url: String)(implicit ec: ExecutionContext): Future[Int] = withErrorHandling("HEAD", url)(_.status)


}
