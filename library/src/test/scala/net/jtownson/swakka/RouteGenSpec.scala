package net.jtownson.swakka

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.HttpMethods.{GET, POST, PUT}
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpRequest}
import akka.http.scaladsl.model.StatusCodes.{NotFound, OK}
import akka.http.scaladsl.server.Route.seal
import akka.http.scaladsl.server._
import akka.http.scaladsl.server.Directives.{complete, reject}
import akka.http.scaladsl.testkit.{RouteTest, TestFrameworkInterface}
import org.scalamock.scalatest.MockFactory
import org.scalatest.FlatSpec
import org.scalatest.Inside._
import org.scalatest.Matchers._
import org.scalatest.prop.TableDrivenPropertyChecks._
import shapeless.{::, HList, HNil}
import spray.json._
import net.jtownson.swakka.OpenApiModel._
import net.jtownson.swakka.model.Parameters.{BodyParameter, PathParameter, QueryParameter}
import net.jtownson.swakka.model.Responses.ResponseValue
import org.scalamock.function.MockFunction1

class RouteGenSpec extends FlatSpec with MockFactory with RouteTest with TestFrameworkInterface {

  import OpenApiJsonProtocol._
  import net.jtownson.swakka.routegen.ConvertibleToDirective._

  def f[Params <: HList] = mockFunction[Params, Route]

  private val defaultOp = Operation[HNil, ResponseValue[String, HNil]](
    parameters = HNil,
    responses = ResponseValue("200", "ok"), endpointImplementation = f)

  val zeroParamModels = Table(
    ("testcase name", "request", "model", "response"),
    ("index page", get("/"), PathItem(path = "/", method = GET, operation = defaultOp), "YES"),
    ("simple path", get("/ruok"), PathItem(path = "/ruok", method = GET, operation = defaultOp), "YES"),
    ("missing base path", get("/ruok"), PathItem(path = "ruok", method = GET, operation = defaultOp), "YES"),
    ("complex path", get("/ruok/json"), PathItem(path = "ruok/json", method = GET, operation = defaultOp), "YES")
  )

  forAll(zeroParamModels) { (testcaseName, request, apiModel, response) =>
    testcaseName should "convert to a complete akka Route" in {

      val implementation = mockImpl(apiModel.operation)
      implementation.expects(*).returning(complete(response))

      val route = RouteGen.pathItemRoute(apiModel)

      request ~> route ~> check {
        status shouldBe OK
        responseAs[String] shouldBe response
      }
    }
  }

  private def mockImpl[Params <: HList, Responses](operation: Operation[Params, Responses]):
  MockFunction1[Params, Route] =
    operation.endpointImplementation.asInstanceOf[MockFunction1[Params, Route]]

  type OneStringParam = QueryParameter[String] :: HNil

  private val opWithQueryParam = Operation(
    parameters = QueryParameter[String]('q) :: HNil,
    responses = ResponseValue[String, HNil]("200", "ok"), endpointImplementation = f)

  val oneStrParamModels = Table(
    ("testcase name", "request", "model", "response"),
    ("echo query", get("/app?q=x"), PathItem(path = "/app", method = GET, operation = opWithQueryParam), "x")
  )

  forAll(oneStrParamModels) { (testcaseName, request, apiModel, response) =>
    testcaseName should "convert to a complete akka Route" in {

      mockImpl(apiModel.operation).expects(*).returning(complete(response))

      val route = RouteGen.pathItemRoute(apiModel)

      request ~> route ~> check {
        status shouldBe OK
        responseAs[String] shouldBe response
      }
    }
  }

  type OneIntParam = QueryParameter[Int] :: HNil

  val opWithIntParam = Operation(
    parameters = QueryParameter[Int]('q) :: HNil,
    responses = ResponseValue[String, HNil]("200", "ok"), endpointImplementation = f)

  "int params that are NOT ints" should "be rejected" in {

    val request = get("/app?q=x")

    val route = RouteGen.pathItemRoute(PathItem(path = "/app", method = GET, operation = opWithIntParam))

    request ~> route ~> check {
      inside(rejection) { case MalformedQueryParamRejection(parameterName, _, _) =>
        parameterName shouldBe "q"
      }
    }
  }

  "int params that are ints" should "be passed" in {

    val request = get("/app?q=10")

    mockImpl(opWithIntParam).expects(*).returning(complete("x"))

    val route = RouteGen.pathItemRoute(PathItem(path = "/app", method = GET, operation = opWithIntParam))

    request ~> route ~> check {
      status shouldBe OK
      responseAs[String] shouldBe "x"
    }
  }

  "A PathParam" should "be passed if it matches the model path" in {

    val request = Put("http://foo.com/widgets/12345")

    val op = Operation[PathParameter[Int] :: HNil, HNil](
      parameters = PathParameter[Int]('widgetId) :: HNil,
      endpointImplementation = f)

    mockImpl(op).expects(*).returning(complete("nothing"))

    val route = RouteGen.pathItemRoute(PathItem(path = "/widgets/{widgetId}", method = PUT, operation = op))

    request ~> route ~> check {
      status shouldBe OK
    }
  }

  it should "be rejected if it is of the wrong type" in {
    val request = Put("http://foo.com/widgets/bam")

    val op = Operation[PathParameter[Int] :: HNil, HNil](
      parameters = PathParameter[Int]('widgetId) :: HNil,
      endpointImplementation = f)

    val route = RouteGen.pathItemRoute(PathItem(path = "/widgets/{widgetId}", method = PUT, operation = op))

    request ~> seal(route) ~> check {
      status shouldBe NotFound
    }
  }


  case class Pet(name: String)

  implicit val petFormat = jsonFormat1(Pet)

  type Params = BodyParameter[Pet] :: HNil
  type Responses = ResponseValue[String, HNil]

  val opWithBodyParam = Operation(
    parameters = BodyParameter[Pet]('pet) :: HNil,
    responses = ResponseValue[String, HNil]("200", "ok"), endpointImplementation = f)

  "body params of correct type" should "be marshallable" in {

    val request = post("/app", Pet("tiddles"))
    val animalRoute = RouteGen.pathItemRoute(PathItem(path = "/app", method = POST, operation = opWithBodyParam))
    mockImpl(opWithBodyParam).expects(*).returning(complete("x"))

    request ~> animalRoute ~> check {
      status shouldBe OK
    }
  }

  case class WildAnimal(species: String)

  implicit val wildAnimalFormat = jsonFormat1(WildAnimal)

  "body params of wrong type" should "be rejected" in {

    val animalRoute = RouteGen.pathItemRoute(PathItem(path = "/app", method = POST, operation = opWithBodyParam))

    post("/app", WildAnimal("lion")) ~> animalRoute ~> check {
      inside(rejection) {
        case MalformedRequestContentRejection(message, _) =>
          message shouldBe "Object is missing required member 'name'"
      }
    }
  }

  "body params" should "be easy to handle in endpoint impls" in {

    val tiddles = Pet("tiddles")
    val animalRoute = RouteGen.pathItemRoute(PathItem(path = "/app", method = POST, operation = opWithBodyParam))

    val request = post("/app", tiddles)
    mockImpl(opWithBodyParam).expects(*).returning(complete(tiddles))
    request ~> animalRoute ~> check {
      status shouldBe OK
      responseAs[Pet] shouldBe tiddles
    }
  }

  "multiple paths" should "work" in {

    val f1 = mockFunction[OneIntParam, Route]
    val f2 = mockFunction[OneStringParam, Route]

    type Paths =
      PathItem[OneIntParam, ResponseValue[String, HNil]] ::
        PathItem[OneStringParam, ResponseValue[String, HNil]] :: HNil

    val path1: PathItem[OneIntParam, ResponseValue[String, HNil]] =
      PathItem(
        path = "/app/e1",
        method = GET,
        operation = Operation(
          parameters = QueryParameter[Int]('q) :: HNil,
          responses = ResponseValue[String, HNil]("200", "ok"), endpointImplementation = f1))

    val path2: PathItem[OneStringParam, ResponseValue[String, HNil]] =
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

  "the swagger route" should "be enabled with a toggle" in {

    type Params = QueryParameter[Int] :: HNil
    type Responses = ResponseValue[String, HNil]
    type Paths = PathItem[Params, Responses]

    val f = mockFunction[Params, Route]

    val api = OpenApi[Paths](paths =
      PathItem(
        path = "/app/e1",
        method = GET,
        operation = Operation(
          parameters = QueryParameter[Int]('q) :: HNil,
          responses = ResponseValue[String, HNil]("200", "ok"), endpointImplementation = f)))

    val route = RouteGen.openApiRoute(api, includeSwaggerRoute = true)

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
    type NoParams = HNil
    type StringResponse = ResponseValue[String, HNil]

    type Paths = PathItem[NoParams, StringResponse] :: HNil

    val api =
      OpenApi(
        paths =
          PathItem[NoParams, StringResponse](
            path = "/app/e1",
            method = GET,
            operation = Operation[NoParams, StringResponse](
              responses = ResponseValue[String, HNil]("200", "ok"),
              endpointImplementation = _ => complete("pong")
            )
          ) ::
          HNil
      )

    val route: Route = RouteGen.openApiRoute(api, includeSwaggerRoute = true)

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

    type Params = PathParameter[String] :: HNil
    type StringResponse = ResponseValue[String, HNil]
    type Paths = PathItem[Params, StringResponse] :: HNil

    val greet: Params => Route = {
      case (nameParameter :: HNil) =>
        complete(s"Hello ${nameParameter.value}!")
    }

    val api =
      OpenApi(paths =
        PathItem(
          path = "/greet/{name}",
          method = GET,
          operation = Operation[Params, StringResponse](
            parameters = PathParameter[String]('name) :: HNil,
            responses = ResponseValue[String, HNil]("200", "ok"),
            endpointImplementation = greet
          )
        ) ::
          HNil
      )

    val route: Route = RouteGen.openApiRoute(api, includeSwaggerRoute = true)

    Get("http://localhost:8080/greet/Katharine") ~> seal(route) ~> check {
      status shouldBe OK
      responseAs[String] shouldBe "Hello Katharine!"
    }
  }

  "host element" should "be included in the route defn" in {
    type Paths = PathItem[HNil, HNil]

    val f = mockFunction[HNil, Route]

    val api = OpenApi[Paths](
      host = Some("foo"),
      paths =
        PathItem(path =
          "/app", method = GET, operation = Operation(parameters = HNil, responses = HNil, endpointImplementation = f)))

    val route = RouteGen.openApiRoute(api)

    val request0 = get("bar", "/app")
    request0 ~> seal(route) ~> check {
      status shouldBe NotFound
    }

    val request1 = get("foo", "/app")
    f.expects(*).returning(complete("x"))
    request1 ~> route ~> check {
      status shouldBe OK
    }
  }

  "schemes" should "be included in the route defn" in {
    type Paths = PathItem[HNil, HNil]

    val f = mockFunction[HNil, Route]

    val api = OpenApi[Paths](
      schemes = Some(Seq("http")),
      paths =
        PathItem(
          path = "/app",
          method = GET,
          operation = Operation(
            parameters = HNil,
            responses = HNil,
            endpointImplementation = f))
    )

    val route = RouteGen.openApiRoute(api, includeSwaggerRoute = true)

    val request0 = Get("https://foo.com/app")
    request0 ~> route ~> check {
      inside(rejection) {
        case SchemeRejection(scheme) => scheme shouldBe "http"
      }
    }

    val request1 = Get("http://foo.com/app")
    f.expects(*).returning(complete("x"))
    request1 ~> route ~> check {
      status shouldBe OK
    }
  }

  "optional query parameters, when missing" should "not cause rejections" in {

    type Params = QueryParameter[Option[Int]] :: HNil
    type Responses = ResponseValue[String, HNil]
    type Paths = PathItem[Params, Responses]

    val f: Params => Route = {
      case (qp :: HNil) => {
        qp.value match {
          case None => complete("Ok")
          case _ => reject
        }
      }
    }

    val api = OpenApi[Paths](paths =
      PathItem(
        path = "/app/e1",
        method = GET,
        operation = Operation(
          parameters = QueryParameter[Option[Int]]('q, required = false) :: HNil,
          responses = ResponseValue[String, HNil]("200", "ok"),
          endpointImplementation = f)))

    val route = RouteGen.openApiRoute(api)

    Get("http://localhost:8080/app/e1") ~> seal(route) ~> check {
      status shouldBe OK
      responseAs[String] shouldBe "Ok"
    }
  }

  "optional query parameters, when missing" should "be completed with a default value if one is available" in {

    type Params = QueryParameter[String] :: HNil
    type Responses = ResponseValue[String, HNil]
    type Paths = PathItem[Params, Responses]

    val f: Params => Route = {
      case (qp :: HNil) =>
        if (qp.value == "the-default")
          complete("Ok")
        else
          reject
    }

    val api = OpenApi[Paths](paths =
      PathItem(
        path = "/app/e1",
        method = GET,
        operation = Operation(
          parameters = QueryParameter[String]('q, required = false, default = Some("the-default")) :: HNil,
          responses = ResponseValue[String, HNil]("200", "ok"),
          endpointImplementation = f)))

    val route = RouteGen.openApiRoute(api)

    Get("http://localhost:8080/app/e1") ~> seal(route) ~> check {
      status shouldBe OK
      responseAs[String] shouldBe "Ok"
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
