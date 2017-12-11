/*
 * Copyright 2017 Jeremy Townson
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

package net.jtownson.swakka.coreroutegen

import akka.http.scaladsl.model.HttpHeader
import akka.http.scaladsl.model.headers.RawHeader

import scala.collection.immutable.Seq

trait CorsUseCases {

  val NoCors = new CorsUseCase {
    def headers = Seq()
  }

  val SwaggerUiOnSameHostAsApplication = NoCors

  val CorsHandledByProxyServer = NoCors

  case class SpecificallyThese(headers: Seq[HttpHeader]) extends CorsUseCase

  case class CustomCors(accessControlAllowOrigin: String,
                        accessControlAllowMethods: Seq[String],
                        accessControlAllowHeaders: Seq[String])
      extends CorsUseCase {

    private def asHeader(header: String)(vals: Seq[String]): RawHeader =
      RawHeader(header, vals.mkString(","))

    override def headers: Seq[HttpHeader] = {
      Seq(
        Some(
          RawHeader("Access-Control-Allow-Origin", accessControlAllowOrigin)),
        Option(accessControlAllowMethods)
          .filter(_.nonEmpty)
          .map(asHeader("Access-Control-Allow-Methods")),
        Option(accessControlAllowHeaders)
          .filter(_.nonEmpty)
          .map(asHeader("Access-Control-Allow-Headers"))
      ).flatten
    }
  }
}
