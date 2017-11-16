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

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.HttpMethods.{GET, POST, PUT}
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpRequest}
import akka.http.scaladsl.model.StatusCodes.{NotFound, OK}
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.Route.seal
import akka.http.scaladsl.server._
import akka.http.scaladsl.server.Directives.{complete, optionalHeaderValueByName, reject}
import akka.http.scaladsl.testkit.{RouteTest, TestFrameworkInterface}
import org.scalamock.scalatest.MockFactory
import org.scalatest.FlatSpec
import org.scalatest.Inside._
import org.scalatest.Matchers._
import org.scalatest.prop.TableDrivenPropertyChecks._
import shapeless.{::, HNil}
import spray.json._
import net.jtownson.swakka.OpenApiModel._
import net.jtownson.swakka.model.Parameters.{BodyParameter, PathParameter, QueryParameter}
import net.jtownson.swakka.model.Responses.ResponseValue
import net.jtownson.swakka.routegen.SwaggerRouteSettings
import org.scalamock.function.{MockFunction0, MockFunction1}
import net.jtownson.swakka.model.Invoker._
import net.jtownson.swakka.model.ParameterValue._

import scala.collection.immutable.Seq

class RouteGenSpec extends FlatSpec with MockFactory with RouteTest with TestFrameworkInterface {

  import OpenApiJsonProtocol._
  import net.jtownson.swakka.routegen.ConvertibleToDirective._

  private def f0: MockFunction0[Route] = mockFunction[Route]

  private def defaultOp = Operation[HNil, () => Route, ResponseValue[String, HNil]](
    responses = ResponseValue("200", "ok"), endpointImplementation = f0)

  val zeroParamModels = Table(
    ("testcase name", "request", "model", "response"),
    ("index page", get("/"), PathItem(path = "/", method = GET, operation = defaultOp), "YES"),
    ("simple path", get("/ruok"), PathItem(path = "/ruok", method = GET, operation = defaultOp), "YES"),
    ("missing base path", get("/ruok"), PathItem(path = "ruok", method = GET, operation = defaultOp), "YES"),
    ("complex path", get("/ruok/json"), PathItem(path = "ruok/json", method = GET, operation = defaultOp), "YES")
  )

  forAll(zeroParamModels) { (testcaseName, request, apiModel, response) =>
    testcaseName should "convert to a complete akka Route" in {

      val endpoint: MockFunction0[Route] = apiModel.operation.endpointImplementation.asInstanceOf[MockFunction0[Route]]

      endpoint.expects().returning(complete(response))

      val route = RouteGen.pathItemRoute(apiModel)

      request ~> route ~> check {
        status shouldBe OK
        responseAs[String] shouldBe response
      }
    }
  }

  def f1s = mockFunction[String, Route]

  private val opWithQueryParam = Operation(
    parameters = QueryParameter[String]('q) :: HNil,
    responses = ResponseValue[String, HNil]("200", "ok"), endpointImplementation = f1s)

  val oneStrParamModels = Table(
    ("testcase name", "request", "model", "response"),
    ("echo query", get("/app?q=x"), PathItem(path = "/app", method = GET, operation = opWithQueryParam), "x")
  )

  forAll(oneStrParamModels) { (testcaseName, request, apiModel, response) =>
    testcaseName should "convert to a complete akka Route" in {

      apiModel.operation.endpointImplementation.expects(*).returning(complete(response))

      val route = RouteGen.pathItemRoute(apiModel)

      request ~> route ~> check {
        status shouldBe OK
        responseAs[String] shouldBe response
      }
    }
  }

  def opWithIntParam(endpointImplementation: MockFunction1[Int, Route]) = Operation(
    parameters = QueryParameter[Int]('q) :: HNil,
    responses = ResponseValue[String, HNil]("200", "ok"), endpointImplementation = endpointImplementation)

  "int params that are NOT ints" should "be rejected" in {

    val endpointImplementation = mockFunction[Int, Route]
    val request = get("/app?q=x")

    val route = RouteGen.pathItemRoute(PathItem(path = "/app", method = GET, operation = opWithIntParam(endpointImplementation)))

    request ~> route ~> check {
      inside(rejection) { case MalformedQueryParamRejection(parameterName, _, _) =>
        parameterName shouldBe "q"
      }
    }
  }

  "int params that are ints" should "be passed" in {

    val endpointImplementation = mockFunction[Int, Route]
    val operation = opWithIntParam(endpointImplementation)
    val request = get("/app?q=10")

    endpointImplementation.expects(10).returning(complete("x"))

    val route = RouteGen.pathItemRoute(PathItem(path = "/app", method = GET, operation = operation))

    request ~> route ~> check {
      status shouldBe OK
      responseAs[String] shouldBe "x"
    }
  }

  "A PathParam" should "be passed if it matches the model path" in {

    val request = Put("http://foo.com/widgets/12345")

    val f = mockFunction[Int, Route]

    val op = Operation[PathParameter[Int] :: HNil, Int => Route, HNil](
      parameters = PathParameter[Int]('widgetId) :: HNil,
      endpointImplementation = f)

    f.expects(12345).returning(complete("some response"))

    val route = RouteGen.pathItemRoute(PathItem(path = "/widgets/{widgetId}", method = PUT, operation = op))

    request ~> route ~> check {
      status shouldBe OK
    }
  }

  it should "be rejected if it is of the wrong type" in {
    val request = Put("http://foo.com/widgets/bam")

    val f: Int => Route = mockFunction[Int, Route]

    val op = Operation[PathParameter[Int] :: HNil, Int => Route, HNil](
      parameters = PathParameter[Int]('widgetId) :: HNil,
      endpointImplementation = f)

    val route = RouteGen.pathItemRoute(PathItem(path = "/widgets/{widgetId}", method = PUT, operation = op))

    request ~> seal(route) ~> check {
      status shouldBe NotFound
    }
  }


  case class Pet(name: String)

  implicit val petFormat = jsonFormat1(Pet)

  def opWithBodyParam(endpointImplementation: MockFunction1[Pet, Route]) = Operation(
    parameters = BodyParameter[Pet]('pet) :: HNil,
    responses = ResponseValue[String, HNil]("200", "ok"), endpointImplementation = endpointImplementation)

  "body params of correct type" should "be marshallable" in {

    val bodyParamOp = mockFunction[Pet, Route]

    val operation = opWithBodyParam(bodyParamOp)

    val request = post("/app", Pet("tiddles"))
    bodyParamOp.expects(*).returning(complete("x"))

    val animalRoute = RouteGen.pathItemRoute(PathItem(path = "/app", method = POST, operation = operation))

    request ~> animalRoute ~> check {
      status shouldBe OK
    }
  }

  case class WildAnimal(species: String)

  implicit val wildAnimalFormat = jsonFormat1(WildAnimal)

  "body params of wrong type" should "be rejected" in {

    val operation = opWithBodyParam(mockFunction[Pet, Route])

    val animalRoute = RouteGen.pathItemRoute(PathItem(path = "/app", method = POST, operation = operation))

    post("/app", WildAnimal("lion")) ~> animalRoute ~> check {
      inside(rejection) {
        case MalformedRequestContentRejection(message, _) =>
          message shouldBe "Object is missing required member 'name'"
      }
    }
  }

  "body params" should "be easy to handle in endpoint impls" in {

    val bodyParamOp = mockFunction[Pet, Route]
    val operation = opWithBodyParam(bodyParamOp)
    val tiddles = Pet("tiddles")
    val animalRoute = RouteGen.pathItemRoute(PathItem(path = "/app", method = POST, operation = operation))

    val request = post("/app", tiddles)
    bodyParamOp.expects(*).returning(complete(tiddles))
    request ~> animalRoute ~> check {
      status shouldBe OK
      responseAs[Pet] shouldBe tiddles
    }
  }

  "multiple paths" should "work" in {

    val f1 = mockFunction[Int, Route]
    val f2 = mockFunction[String, Route]

    val path1 =
      PathItem(
        path = "/app/e1",
        method = GET,
        operation = Operation(
          parameters = QueryParameter[Int]('q) :: HNil,
          responses = ResponseValue[String, HNil]("200", "ok"), endpointImplementation = f1))

    val path2 =
      PathItem(
        path = "/app/e2",
        method = GET,
        operation = Operation(
          parameters = QueryParameter[String]('q) :: HNil,
          responses = ResponseValue[String, HNil]("200", "ok"), endpointImplementation = f2))


    val api = OpenApi(paths = path1 :: path2 :: HNil)

    val route = RouteGen.openApiRoute(api)

    val e1Request = get("/app/e1?q=10")
    val e2Request = get("/app/e2?q=str")

    f1.expects(*).returning(complete("e1-response"))
    f2.expects(*).returning(complete("e2-response"))

    e1Request ~> route ~> check {
      status shouldBe OK
      responseAs[String] shouldBe "e1-response"
    }

    e2Request ~> route ~> check {
      status shouldBe OK
      responseAs[String] shouldBe "e2-response"
    }
  }

  "the swagger route" should "be enabled by providing settings" in {

    val f = mockFunction[Int, Route]

    val api = OpenApi(paths =
      PathItem(
        path = "/app/e1",
        method = GET,
        operation = Operation(
          parameters = QueryParameter[Int]('q) :: HNil,
          responses = ResponseValue[String, HNil]("200", "ok"), endpointImplementation = f)))

    val route = RouteGen.openApiRoute(api, Some(SwaggerRouteSettings()))

    val swaggerRequest = get("/swagger.json")

    swaggerRequest ~> route ~> check {
      status shouldBe OK
      responseAs[String] shouldBe api.toJson.prettyPrint
    }

    Get("http://localhost:8080/") ~> seal(route) ~> check {
      status shouldBe NotFound
    }
  }

  "routes with no params" should "reject invalid paths" in {

    val api =
      OpenApi(
        paths =
          PathItem(
            path = "/app/e1",
            method = GET,
            operation = Operation(
              responses = ResponseValue[String, HNil]("200", "ok"),
              endpointImplementation = () => complete("pong")
            )
          ) ::
          HNil
      )

    val route: Route = RouteGen.openApiRoute(api, Some(SwaggerRouteSettings()))

    Get("http://localhost:8080/") ~> seal(route) ~> check {
      status shouldBe NotFound
      responseAs[String] shouldNot be("pong")
    }
    Get("http://localhost:8080/app/e1") ~> seal(route) ~> check {
      status shouldBe OK
      responseAs[String] shouldBe("pong")
    }
  }

  "routes with a path param" should "not reject valid paths" in {

    val greet: String => Route =
      name => complete(s"Hello $name!")

    val api =
      OpenApi(paths =
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

    val route: Route = RouteGen.openApiRoute(api, Some(SwaggerRouteSettings()))

    Get("http://localhost:8080/greet/Katharine") ~> seal(route) ~> check {
      status shouldBe OK
      responseAs[String] shouldBe "Hello Katharine!"
    }
  }

  "host element" should "be included in the route defn" in {
    val f = mockFunction[Route]

    val api = OpenApi(
      host = Some("foo"),
      paths =
        PathItem(
          path = "/app",
          method = GET,
          operation = Operation(
            endpointImplementation = f)))

    val route = RouteGen.openApiRoute(api)

    val request0 = get("bar", "/app")
    request0 ~> seal(route) ~> check {
      status shouldBe NotFound
    }

    val request1 = get("foo", "/app")
    f.expects().returning(complete("x"))
    request1 ~> route ~> check {
      status shouldBe OK
    }
  }

  "if the host element includes a port, it" should "be removed from the route defn" in {
    val f = mockFunction[Route]

    val api = OpenApi(
      host = Some("foo:8080"),
      paths =
        PathItem(
          path = "/app",
          method = GET,
          operation = Operation(
            endpointImplementation = f)))

    val route = RouteGen.openApiRoute(api)

    f.expects().returning(complete("x"))

    val request0 = get("foo", "/app")
    request0 ~> seal(route) ~> check {
      status shouldBe OK
    }

    f.expects().returning(complete("x"))

    val request1 = get("foo:8080", "/app")
    request1 ~> route ~> check {
      status shouldBe OK
    }
  }

  "schemes" should "be included in the route defn" in {
    val f = mockFunction[Route]

    val api = OpenApi(
      schemes = Some(Seq("http")),
      paths =
        PathItem(
          path = "/app",
          method = GET,
          operation = Operation(
            endpointImplementation = f))
    )

    val route = RouteGen.openApiRoute(api, Some(SwaggerRouteSettings()))

    val request0 = Get("https://foo.com/app")
    request0 ~> route ~> check {
      inside(rejection) {
        case SchemeRejection(scheme) => scheme shouldBe "http"
      }
    }

    val request1 = Get("http://foo.com/app")
    f.expects().returning(complete("x"))
    request1 ~> route ~> check {
      status shouldBe OK
    }
  }

  "optional query parameters, when missing" should "not cause rejections" in {

    val f: Option[Int] => Route = {
      case Some(_) =>
        reject
      case None =>
        complete("Ok")
    }

    val api = OpenApi(paths =
      PathItem(
        path = "/app/e1",
        method = GET,
        operation = Operation(
          parameters = QueryParameter[Option[Int]]('q) :: HNil,
          responses = ResponseValue[String, HNil]("200", "ok"),
          endpointImplementation = f)))

    val route = RouteGen.openApiRoute(api)

    Get("http://localhost:8080/app/e1") ~> seal(route) ~> check {
      status shouldBe OK
      responseAs[String] shouldBe "Ok"
    }
  }

  "optional query parameters, when missing" should "be completed with a default value if one is available" in {

    val f: String => Route = value =>
        if (value == "the-default")
          complete("Ok")
        else
          reject

    val api = OpenApi(paths =
      PathItem(
        path = "/app/e1",
        method = GET,
        operation = Operation(
          parameters = QueryParameter[String]('q, default = Some("the-default")) :: HNil,
          responses = ResponseValue[String, HNil]("200", "ok"),
          endpointImplementation = f)))

    val route = RouteGen.openApiRoute(api)

    Get("http://localhost:8080/app/e1") ~> seal(route) ~> check {
      status shouldBe OK
      responseAs[String] shouldBe "Ok"
    }
  }

  "Endpoints" should "support private Akka directives" in {

    val f: () => Route = () =>
      optionalHeaderValueByName("x-forwarded-for") {
        case Some(forward) => complete(s"x-forwarded-for = $forward")
        case None => complete("no x-forwarded-for header set")
      }

    val api = OpenApi(paths =
      PathItem(
        path = "/app",
        method = GET,
        operation = Operation(
          endpointImplementation = f)))

    import net.jtownson.swakka.routegen.SwaggerRouteSettings
    import net.jtownson.swakka.routegen.CorsUseCases._

    val corsHeaders = Seq(
      RawHeader("Access-Control-Allow-Origin", "*"),
      RawHeader("Access-Control-Allow-Methods", "GET"))

    val route = RouteGen.openApiRoute(
      api,
      Some(SwaggerRouteSettings(
        endpointPath = "my/path/to/my/swagger-file.json",
        corsUseCase = SpecificallyThese(corsHeaders))))

    Get("http://localhost:8080/app").withHeaders(RawHeader("x-forwarded-for", "client, proxy1")) ~> seal(route) ~> check {
      responseAs[String] shouldBe "x-forwarded-for = client, proxy1"
    }

    Get("http://localhost:8080/app") ~> seal(route) ~> check {
      responseAs[String] shouldBe "no x-forwarded-for header set"
    }
  }

  private def get(path: String): HttpRequest = {
    get("example.com", path)
  }

  private def get(host: String, path: String): HttpRequest = {
    Get(s"http://$host$path")
  }

  private def post[T: JsonWriter](path: String, t: T): HttpRequest = {
    HttpRequest(POST,
      uri = "http://example.com/app",
      entity = HttpEntity(ContentTypes.`application/json`, t.toJson.prettyPrint))
  }

  override def failTest(msg: String): Nothing = throw new AssertionError(msg)
}
