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
import shapeless.HNil

object OpenApiModel {

  /**
    * Maps to an OpenAPI operation.
    * @param parameters parameters must be an HList of parameter types found in net.jtownson.swakka.model.Parameters
    * @param responses responses must a HList of or bare net.jtownson.swakka.model.Responses.ResponseValue
    * @param security
    * @param endpointImplementation a function of type EndpointImplementation (see below)
    * @tparam Params this is a HList of the parameters collected by your api operation.
    *                e.g. QueryParameter[String] :: HeaderParameter[Long] :: HNil.
    *                These Params affect both the generated swagger json and generated akka-http
    *                Routes which extracts parameters from a user request.
    * @tparam EndpointFunction this is a FunctionN, whose input is dependent on the Params type and returning an akka-http Route.
    *           Given the example params, above, F would be (String, Long) => Route
    * @tparam Responses this is a HList or bare type that defines the responses swagger json.
    */
  case class Operation[Params, EndpointFunction, Responses](
    summary: Option[String] = None,
    description: Option[String] = None,
    operationId: Option[String] = None,
    tags: Option[Seq[String]] = None,
    consumes: Option[Seq[String]] = None,
    produces: Option[Seq[String]] = None,
    parameters: Params = HNil: HNil,
    responses: Responses = HNil: HNil,
    security: Option[Seq[SecurityRequirement]] = None,
    endpointImplementation: EndpointFunction)

  case class PathItem[Params, EndpointFunction, Responses](
    path: String,
    method: HttpMethod,
    operation: Operation[Params, EndpointFunction, Responses])

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

