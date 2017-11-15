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
import shapeless.{HList, HNil, :: => hcons}
import OpenApiModel._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.directives.RouteDirectives
import net.jtownson.swakka.model.Invoker.AkkaHttpInvoker
import net.jtownson.swakka.routegen.SwaggerRoute.swaggerRoute
import net.jtownson.swakka.routegen._
import spray.json.JsonFormat

trait RouteGen[T] {
  def toRoute(t: T): Route
}

object RouteGen {

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

  implicit def hconsRouteGen[H, T <: HList](implicit ev1: RouteGen[H], ev2: RouteGen[T]): RouteGen[hcons[H, T]] =
    (l: hcons[H, T]) => ev1.toRoute(l.head) ~ ev2.toRoute(l.tail)

  implicit def pathItemRouteGen[F, Params <: HList : ConvertibleToDirective, Responses]
  (implicit ev: AkkaHttpInvoker[Params, F]): RouteGen[PathItem[F, Params, Responses]] =
    (pathItem: PathItem[F, Params, Responses]) => pathItemRoute(pathItem)

  implicit val hNilRouteGen: RouteGen[HNil] =
    _ => RouteDirectives.reject

  def pathItemRoute[F, Params <: HList : ConvertibleToDirective, Responses](pathItem: PathItem[F, Params, Responses])
                                                                           (implicit ev: AkkaHttpInvoker[Params, F]): Route =
    pathItemRoute(pathItem.method, pathItem.path, pathItem.operation)

  private def pathItemRoute[F, Params <: HList : ConvertibleToDirective, Responses]
  (httpMethod: HttpMethod, modelPath: String, operation: Operation[F, Params, Responses])
  (implicit ev1: ConvertibleToDirective[Params], ev2: AkkaHttpInvoker[Params, F]) = {

    method(httpMethod) {

      ev1.convertToDirective(modelPath, operation.parameters) { params =>
        ev2.apply(operation.endpointImplementation, params)
      }
    }
  }
}
