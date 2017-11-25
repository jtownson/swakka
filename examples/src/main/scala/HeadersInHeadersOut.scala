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
import akka.http.scaladsl.model.StatusCodes.NoContent
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer

import net.jtownson.swakka.jsonprotocol._
import net.jtownson.swakka.openapimodel._
import net.jtownson.swakka.routegen._

import shapeless.{::, HNil}

import scala.collection.immutable.Seq

// Shows how to declare
// 1. an endpoint that accepts parameters in headers and returns headers in the response
// 2. extract request information that you do not wish to declare in the swagger definition
//
// Usage: curl -i -H'x-header-in: 3.14' http://localhost:8080/
object HeadersInHeadersOut extends App {

  implicit val system = ActorSystem()
  implicit val mat = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val corsHeaders = Seq(
    RawHeader("Access-Control-Allow-Origin", "*"),
    RawHeader("Access-Control-Allow-Methods", "GET"))

  val multiplyInputBy2: Double => Route = number => {

      val ret = (number * 2).toString

      complete(HttpResponse(
        NoContent,
        corsHeaders :+ RawHeader("x-header-out", ret))
      )
    }

  val api =
    OpenApi(
      paths =
      PathItem(
        path = "/",
        method = GET,
        operation = Operation(
          parameters = HeaderParameter[Double](Symbol("x-header-in")) :: HNil,
          responses = ResponseValue[Unit, Header[Double]](
            responseCode = "204",
            description = "the input x-header-in parameter will be multiplied by 2 and returned in x-header-out",
            headers = Header[Double](Symbol("x-header-out"), Some("the value of x-header-in multiplied by 2"))),
          endpointImplementation = multiplyInputBy2
        )
      )
    )

  val route: Route = openApiRoute(api, Some(SwaggerRouteSettings(
    corsUseCase = SpecificallyThese(corsHeaders))))

  val bindingFuture = Http().bindAndHandle(
    route,
    "localhost",
    8080)
}
