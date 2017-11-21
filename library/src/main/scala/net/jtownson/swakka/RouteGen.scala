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
import akka.http.scaladsl.server._
import shapeless.{HList, HNil, ::}
import OpenApiModel._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.directives.RouteDirectives
import net.jtownson.swakka.model.Invoker.AkkaHttpInvoker
import net.jtownson.swakka.routegen.SwaggerRoute.swaggerRoute
import net.jtownson.swakka.routegen.{hostDirective, _}
import spray.json.JsonFormat

/**
  * RouteGen is a type class that supports the conversion of an OpenApi model into a Akka-Http Route.
  * This allows the processing of an HTTP request according to a Swagger definition.
  * See also ConvertibleToDirective.
  * @tparam T
  */
trait RouteGen[T] {
  def toRoute(t: T): Route
}

object RouteGen {

  /**
    * This is the hook to generate a Route for an OpenApi definition.
    * @param api the swagger model
    * @param swaggerRouteSettings a settings object that dictates whether to include the swagger file itself in the Route
    *                             (along with the URL at which to serve it and which CORS headers to set).
    * @tparam Paths A Paths HList. Please refer to the the project readme or sample code for usage.
    * @tparam SecurityDefinitions A HList of SecurityDefinitions. Again, please refer to the readme or samples.
    * @return An Akka-Http Route that extracts the parameters in the api definition, passing them to the endpoints
    *         defined therein.
    */

  def openApiRoute[Paths, SecurityDefinitions]
  (api: OpenApi[Paths, SecurityDefinitions], swaggerRouteSettings: Option[SwaggerRouteSettings] = None)
  (implicit ev1: RouteGen[Paths], ev2: JsonFormat[OpenApi[Paths, SecurityDefinitions]]): Route =
    hostDirective(api.host) {
      schemesDirective(api.schemes) {
        basePathDirective(api.basePath) {
          swaggerRouteSettings match {
            case Some(settings) => ev1.toRoute(api.paths) ~ swaggerRoute(api, settings)
            case None => ev1.toRoute(api.paths)
          }
        }
      }
    }

  implicit def hconsRouteGen[H, T <: HList](
      implicit ev1: RouteGen[H],
      ev2: RouteGen[T]): RouteGen[H :: T] =
    (l: H :: T) => ev1.toRoute(l.head) ~ ev2.toRoute(l.tail)

  implicit def pathItemRouteGen[Params <: HList: ConvertibleToDirective,
                                EndpointFunction,
                                Responses](
      implicit ev: AkkaHttpInvoker[Params, EndpointFunction])
    : RouteGen[PathItem[Params, EndpointFunction, Responses]] =
    (pathItem: PathItem[Params, EndpointFunction, Responses]) =>
      pathItemRoute(pathItem)

  implicit val hNilRouteGen: RouteGen[HNil] =
    (_: HNil) => RouteDirectives.reject

  def pathItemRoute[Params <: HList: ConvertibleToDirective,
                    EndpointFunction,
                    Responses](
      pathItem: PathItem[Params, EndpointFunction, Responses])(
      implicit ev: AkkaHttpInvoker[Params, EndpointFunction]): Route =
    pathItemRoute(pathItem.method, pathItem.path, pathItem.operation)

  private def pathItemRoute[Params <: HList: ConvertibleToDirective,
                            EndpointFunction,
                            Responses](
      httpMethod: HttpMethod,
      modelPath: String,
      operation: Operation[Params, EndpointFunction, Responses])(
      implicit ev1: ConvertibleToDirective[Params],
      ev2: AkkaHttpInvoker[Params, EndpointFunction]) = {

    method(httpMethod) {

      ev1.convertToDirective(modelPath, operation.parameters) { params =>
        ev2.apply(operation.endpointImplementation, params)
      }
    }
  }
}
