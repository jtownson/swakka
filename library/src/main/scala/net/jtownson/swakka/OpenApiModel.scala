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

package net.jtownson.swakka

import akka.http.scaladsl.model.HttpMethod
import net.jtownson.swakka.model.{Info, Tag}
import net.jtownson.swakka.model.ModelDefaults._
import net.jtownson.swakka.model.SecurityDefinitions.SecurityRequirement
import net.jtownson.swakka.routegen.ConvertibleToDirective
import shapeless.HNil

object OpenApiModel {

  /**
    *
    * @param summary
    * @param description
    * @param operationId
    * @param tags
    * @param consumes
    * @param produces
    * @param parameters
    * @param responses
    * @param security
    * @param endpointImplementation
    * @tparam Params this is a HList of the parameters collected by your api operation.
    *                e.g. QueryParameter[String] :: HeaderParameter[Long] :: HNil.
    *                These Params affect both the generated swagger json and generated akka-http
    *                Routes which extracts parameters from a user request.
    * @tparam F this is a FunctionN, whose input is dependent on the Params type and returning an akka-http Route.
    *           Given the example params, above, F would be (String, Long) => Route
    * @tparam Responses this is a HList or bare type that defines the responses swagger json.
    */
  case class Operation[F, Params : ConvertibleToDirective, Responses](
    summary: Option[String] = None,
    description: Option[String] = None,
    operationId: Option[String] = None,
    tags: Option[Seq[String]] = None,
    consumes: Option[Seq[String]] = None,
    produces: Option[Seq[String]] = None,
    parameters: Params = HNil,
    responses: Responses = HNil,
    security: Option[Seq[SecurityRequirement]] = None,
    endpointImplementation: F)

  case class PathItem[F, Params : ConvertibleToDirective, Responses](
    path: String,
    method: HttpMethod,
    operation: Operation[F, Params, Responses])

  case class OpenApi[Paths, +SecurityDefinitions] (
    info: Info = pointlessInfo,
    host: Option[String] = None,
    basePath: Option[String] = None,
    tags: Option[Seq[Tag]] = None,
    schemes: Option[Seq[String]] = None,
    consumes: Option[Seq[String]] = None,
    produces: Option[Seq[String]] = None,
    paths: Paths,
    securityDefinitions: Option[SecurityDefinitions] = None)
}

