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

import akka.http.scaladsl.model.HttpMethods.GET
import akka.http.scaladsl.model.StatusCodes.{NotFound, OK}
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{HttpHeader, HttpRequest, StatusCode}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Route.seal
import akka.http.scaladsl.testkit.{RouteTest, TestFrameworkInterface}
import net.jtownson.swakka.OpenApiJsonProtocol
import net.jtownson.swakka.OpenApiModel._
import net.jtownson.swakka.model.Parameters.QueryParameter
import net.jtownson.swakka.model.Responses.ResponseValue
import net.jtownson.swakka.routegen.CorsUseCases.{CorsHandledByProxyServer, CustomCors, NoCors, SwaggerUiOnSameHostAsApplication}
import org.scalamock.scalatest.MockFactory
import org.scalatest.FlatSpec
import org.scalatest.Matchers._
import org.scalatest.prop.TableDrivenPropertyChecks._
import shapeless.{::, HNil}
import spray.json._

import scala.collection.immutable.Seq

class SwaggerRouteSpec extends FlatSpec with MockFactory with RouteTest with TestFrameworkInterface {

  import OpenApiJsonProtocol._

  def f1[T] = mockFunction[T, Route]

  type OneIntParam = QueryParameter[Int] :: HNil
  type OneStrParam = QueryParameter[String] :: HNil

  type StringResponse = ResponseValue[String, HNil]

  type Paths =
    PathItem[OneIntParam, Int => Route, StringResponse] ::
    PathItem[OneStrParam, String => Route, StringResponse] :: HNil

  val api =
    OpenApi[Paths, HNil](paths =
      PathItem[OneIntParam, Int => Route, StringResponse](
        path = "/app/e1",
        method = GET,
        operation = Operation(
          parameters = QueryParameter[Int]('q) :: HNil,
          responses = ResponseValue[String, HNil]("200", "ok"),
          endpointImplementation = f1[Int]
        )
      ) ::
        PathItem[OneStrParam, String => Route, StringResponse](
          path = "/app/e2",
          method = GET,
          operation = Operation(
            parameters = QueryParameter[String]('q) :: HNil,
            responses = ResponseValue[String, HNil]("200", "ok"),
            endpointImplementation = f1[String]
          )
        ) ::
        HNil
    )

  "Swagger route" should "return a swagger file" in {

    val route = SwaggerRoute.swaggerRoute(api, SwaggerRouteSettings())

    Get(s"http://example.com/swagger.json") ~> route ~> check {
      status shouldBe OK
      responseAs[String] shouldBe api.toJson.prettyPrint
    }
  }

  val paths = Table[String, HttpRequest, StatusCode](
    ("path", "request", "expected status"),
    ("/another/swagger-file.json", Get(s"http://example.com/another/swagger-file.json"), OK),
    ("another/swagger-file.json", Get(s"http://example.com/another/swagger-file.json"), OK),
    ("another/swagger-file.json", Get(s"http://example.com/swagger-file.json"), NotFound)
  )

  it should "allow swagger endpoint url to be configured" in {
    forAll(paths) { (path, request, expectedStatus) =>

      val route = SwaggerRoute.swaggerRoute(api, SwaggerRouteSettings(endpointPath = path))

      request ~> seal(route) ~> check {
        status shouldBe expectedStatus
      }
    }
  }

  val corsCases = Table[CorsUseCase, Seq[HttpHeader]](
    ("use case", "expected headers"),
    (NoCors, Seq()),
    (SwaggerUiOnSameHostAsApplication, Seq()),
    (CorsHandledByProxyServer, Seq()),
    (CustomCors("*", Seq("GET"), Seq()),
      Seq(RawHeader("Access-Control-Allow-Origin", "*"),
        RawHeader("Access-Control-Allow-Methods", "GET")))
  )

  it should "return CORS headers as configured" in {
    forAll(corsCases) { (useCase, expectedHeaders) =>

      val route = SwaggerRoute.swaggerRoute(api, SwaggerRouteSettings(corsUseCase = useCase))

      Get(s"http://example.com/swagger.json") ~> route ~> check {
        headers shouldBe expectedHeaders
      }
    }
  }

  override def failTest(msg: String): Nothing = throw new AssertionError(msg)
}
