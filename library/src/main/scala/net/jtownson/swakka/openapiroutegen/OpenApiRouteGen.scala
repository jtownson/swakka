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

package net.jtownson.swakka.openapiroutegen

import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import net.jtownson.swakka.coreroutegen.Invoker.AkkaHttpInvoker
import net.jtownson.swakka.coreroutegen._
import net.jtownson.swakka.openapimodel._
import shapeless.HList

trait OpenApiRouteGen {

  implicit def pathItemRouteGen[RequestParams,
                                EndpointParams,
                                EndpointFunction,
                                Responses](
      implicit ev1: AkkaHttpInvoker[RequestParams, EndpointParams, EndpointFunction],
      ev2: ConvertibleToDirective.Aux[RequestParams, EndpointParams])
    : RouteGen[PathItem[RequestParams, EndpointFunction, Responses]] =
    (pathItem: PathItem[RequestParams, EndpointFunction, Responses]) =>
      pathItemRoute(pathItem)

  def pathItemRoute[RequestParams,
                    EndpointParams,
                    EndpointFunction,
                    Responses](
      pathItem: PathItem[RequestParams, EndpointFunction, Responses])(
      implicit ev1: AkkaHttpInvoker[RequestParams, EndpointParams, EndpointFunction],
      ev2: ConvertibleToDirective.Aux[RequestParams, EndpointParams]): Route =
    pathItemRoute(pathItem.method, pathItem.path, pathItem.operation)

  private def pathItemRoute[RequestParams,
                            EndpointParams,
                            EndpointFunction,
                            Responses](
      httpMethod: HttpMethod,
      modelPath: String,
      operation: Operation[RequestParams, EndpointFunction, Responses])(
                                        implicit converter: ConvertibleToDirective.Aux[RequestParams, EndpointParams],
                                        invoker: AkkaHttpInvoker[RequestParams, EndpointParams, EndpointFunction]) = {

    method(httpMethod) {
      converter.convertToDirective(modelPath, operation.parameters) { params: EndpointParams =>
        invoker.apply(operation.endpointImplementation, params)
      }
    }
  }
}

object OpenApiRouteGen extends OpenApiRouteGen
