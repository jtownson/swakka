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

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods.GET
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCodes.OK
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives.complete
import akka.stream.ActorMaterializer

import scala.collection.immutable.Seq


// Shows how to create
// 1. a simple API, with a single endpoint that takes no parameters
// 2. the corresponding akka route
// 3. the swagger file endpoint at /swagger.json

// Usage: curl -i http://localhost:8080/ping

// Core OpenAPI case classes
import net.jtownson.swakka.openapimodel._

// Generates an akka-http Route from an API definition
import net.jtownson.swakka.coreroutegen._
import net.jtownson.swakka.openapiroutegen._

// Implicit json formats for serializing the swagger.json
import net.jtownson.swakka.openapijson._


object PingPong extends App {

  implicit val system = ActorSystem()
  implicit val mat = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val endpointImplementation: () => Route =
    () => complete(HttpResponse(OK, corsHeaders, "pong"))

  val corsHeaders = Seq(
    RawHeader("Access-Control-Allow-Origin", "*"),
    RawHeader("Access-Control-Allow-Methods", "GET"))

  val api =
    OpenApi(
      produces = Some(Seq("text/plain")),
      paths =
      PathItem(
        path = "/ping",
        method = GET,
        operation = Operation(
          responses = ResponseValue[String]("200", "ok"),
          endpointImplementation = endpointImplementation
        )
      )
    )

  val route: Route = openApiRoute(
    api,
    swaggerRouteSettings = Some(DocRouteSettings(corsUseCase = SpecificallyThese(corsHeaders))))

  val bindingFuture = Http().bindAndHandle(
    route,
    "localhost",
    8080)
}
