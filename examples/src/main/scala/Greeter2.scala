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
import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer

import net.jtownson.swakka.openapijson._
import net.jtownson.swakka.openapimodel._
import net.jtownson.swakka.openapiroutegen._

import shapeless.{::, HNil}

import scala.collection.immutable.Seq

// Shows how to create
// an endpoint that accepts a path parameter

// Usage: curl -i http://localhost:8080/greet/John

object Greeter2 extends App {

  implicit val system = ActorSystem()
  implicit val mat = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val corsHeaders = Seq(
    RawHeader("Access-Control-Allow-Origin", "*"),
    RawHeader("Access-Control-Allow-Methods", "GET"))

  val greet: String => Route =
    name =>
      complete(HttpResponse(OK, corsHeaders, s"Hello ${name}!"))

  val api =
    OpenApi(
      produces = Some(Seq("text/plain")),
      paths =
      PathItem(
        path = "/greet/{name}",
        method = GET,
        operation = Operation(
          parameters = PathParameter[String]('name) :: HNil,
          responses = ResponseValue[String, HNil]("200", "ok"),
          endpointImplementation = greet
        )
      ) ::
        HNil
    )

  val route: Route = openApiRoute(
    api,
    Some(SwaggerRouteSettings(
      corsUseCase = SpecificallyThese(corsHeaders))))

  val bindingFuture = Http().bindAndHandle(
    route,
    "localhost",
    8080)
}
