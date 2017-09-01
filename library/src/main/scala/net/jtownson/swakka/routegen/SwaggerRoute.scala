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

package net.jtownson.swakka.routegen

import akka.http.scaladsl.model.ContentTypes.`application/json`
import akka.http.scaladsl.model.{HttpEntity, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives.{complete, _}
import akka.http.scaladsl.server.Route
import net.jtownson.swakka.OpenApiModel.OpenApi
import net.jtownson.swakka.routegen.PathHandling.pathWithSplit
import spray.json.JsonFormat

object SwaggerRoute {

  def swaggerRoute[Paths, SecurityDefinitions]
  (api: OpenApi[Paths, SecurityDefinitions], swaggerRouteSettings: SwaggerRouteSettings)
                         (implicit ev: JsonFormat[OpenApi[Paths, SecurityDefinitions]]): Route = {

    get {
      pathWithSplit(swaggerRouteSettings.endpointPath) {
        complete(HttpResponse(
          status = StatusCodes.OK,
          headers = swaggerRouteSettings.corsUseCase.headers,
          entity = HttpEntity(`application/json`, ev.write(api).prettyPrint))
        )
      }
    }
  }
}
