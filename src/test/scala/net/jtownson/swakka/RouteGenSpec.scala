package net.jtownson.swakka

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.HttpMethods.{GET, POST}
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpRequest}
import akka.http.scaladsl.model.StatusCodes.{NotFound, OK}
import akka.http.scaladsl.server.Route.seal
import akka.http.scaladsl.server.{MalformedQueryParamRejection, MalformedRequestContentRejection, SchemeRejection}
import akka.http.scaladsl.testkit.{RouteTest, TestFrameworkInterface}
import org.scalamock.scalatest.MockFactory
import org.scalatest.FlatSpec
import org.scalatest.Inside._
import org.scalatest.Matchers._
import org.scalatest.prop.TableDrivenPropertyChecks._
import shapeless.{::, HNil}
import spray.json._
import net.jtownson.swakka.OpenApiModel._

class RouteGenSpec extends FlatSpec with MockFactory with RouteTest with TestFrameworkInterface {

  import OpenApiJsonProtocol._
  import net.jtownson.swakka.routegen.ConvertibleToDirective0._

  val f = mockFunction[HttpRequest, ToResponseMarshallable]

  private val defaultOp = Operation[HNil, ResponseValue[String]](HNil, ResponseValue[String](200), f)

  val zeroParamModels = Table(
    ("testcase name", "request", "model", "response"),
    ("index page", get("/"), PathItem(path = "/", method = GET, operation = defaultOp), "YES"),
    ("simple path", get("/ruok"), PathItem(path = "/ruok", method = GET, operation = defaultOp), "YES"),
    ("missing base path", get("/ruok"), PathItem(path = "ruok", method = GET, operation = defaultOp), "YES"),
    ("complex path", get("/ruok/json"), PathItem(path = "ruok/json", method = GET, operation = defaultOp), "YES")
  )

  forAll(zeroParamModels) { (testcaseName, request, apiModel, response) =>
    testcaseName should "convert to a complete akka Route" in {

      f expects request returning response

      val route = RouteGen.pathItemRoute(apiModel)

      request ~> route ~> check {
        status shouldBe OK
        responseAs[String] shouldBe response
      }
    }
  }

  type OneStringParam = QueryParameter[String] :: HNil

  private val opWithQueryParam = Operation(QueryParameter[String]('q) :: HNil, ResponseValue[String](200) :: HNil, f)

  val oneStrParamModels = Table(
    ("testcase name", "request", "model", "response"),
    ("echo query", get("/app?q=x"), PathItem(path ="/app", method = GET, operation = opWithQueryParam), "x")
  )

  forAll(oneStrParamModels) { (testcaseName, request, apiModel, response) =>
    testcaseName should "convert to a complete akka Route" in {

      f expects request returning response

      val route = RouteGen.pathItemRoute(apiModel)

      request ~> route ~> check {
        status shouldBe OK
        responseAs[String] shouldBe response
      }
    }
  }

  type OneIntParam = QueryParameter[Int] :: HNil

  val opWithIntParam = Operation(QueryParameter[Int]('q) :: HNil, ResponseValue[String](200) :: HNil, f)

  "int params that are NOT ints" should "be rejected" in {

    val request = get("/app?q=x")

    val route = RouteGen.pathItemRoute(PathItem(path ="/app", method = GET, operation = opWithIntParam))

    request ~> route ~> check {
      inside(rejection) { case MalformedQueryParamRejection(parameterName, _, _) =>
        parameterName shouldBe "q"
      }
    }
  }

  "int params that are ints" should "be passed" in {

    val request = get("/app?q=10")

    f expects request returning "x"

    val route = RouteGen.pathItemRoute(PathItem(path ="/app", method = GET, operation = opWithIntParam))

    request ~> route ~> check {
      status shouldBe OK
      responseAs[String] shouldBe "x"
    }
  }

  case class Pet(name: String)

  implicit val petFormat = jsonFormat1(Pet)

  type Params = BodyParameter[Pet] :: HNil
  type Responses = ResponseValue[String] :: HNil

  val opWithBodyParam = Operation(BodyParameter[Pet]('pet) :: HNil, ResponseValue[String](200) :: HNil, f)

  "body params of correct type" should "be marshallable" in {

    val request = post("/app", Pet("tiddles"))
    val animalRoute = RouteGen.pathItemRoute(PathItem(path ="/app", method = POST, operation = opWithBodyParam))
    f expects request returning "x"

    request ~> animalRoute ~> check {
      status shouldBe OK
    }
  }

  case class WildAnimal(species: String)

  implicit val wildAnimalFormat = jsonFormat1(WildAnimal)

  "body params of wrong type" should "be rejected" in {

    val animalRoute = RouteGen.pathItemRoute(PathItem(path ="/app", method = POST, operation = opWithBodyParam))

    post("/app", WildAnimal("lion")) ~> animalRoute ~> check {
      inside(rejection) {
        case MalformedRequestContentRejection(message, _) =>
          message shouldBe "Object is missing required member 'name'"
      }
    }
  }

  "body params" should "be easy to handle in endpoint impls" in {

    val tiddles = Pet("tiddles")
    val animalRoute = RouteGen.pathItemRoute(PathItem(path ="/app", method = POST, operation = opWithBodyParam))

    val request = post("/app", tiddles)
    f expects request returning tiddles
    request ~> animalRoute ~> check {
      status shouldBe OK
      responseAs[Pet] shouldBe tiddles
    }
  }

  "multiple paths" should "work" in {

    val f1 = mockFunction[HttpRequest, ToResponseMarshallable]
    val f2 = mockFunction[HttpRequest, ToResponseMarshallable]

    type Paths =
      PathItem[OneIntParam, ::[ResponseValue[String], HNil]] ::
        PathItem[OneStringParam, ::[ResponseValue[String], HNil]] :: HNil

    val path1: PathItem[OneIntParam, ::[ResponseValue[String], HNil]] = PathItem(path =
      "/app/e1", method = GET, operation = Operation(QueryParameter[Int]('q) :: HNil, ResponseValue[String](200) :: HNil, f1))

    val path2: PathItem[OneStringParam, ::[ResponseValue[String], HNil]] = PathItem(path =
      "/app/e2", method = GET, operation = Operation(QueryParameter[String]('q) :: HNil, ResponseValue[String](200) :: HNil, f2))


    val api = OpenApi(paths = path1 :: path2 :: HNil)

    implicit val apiJsonFormat = apiFormat[Paths]
    val route = RouteGen.openApiRoute(api)

    val e1Request = get("/app/e1?q=10")
    val e2Request = get("/app/e2?q=str")

    f1 expects e1Request returning "e1-response"
    f2 expects e2Request returning "e2-response"

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
    type Responses = ResponseValue[String]
    type Paths = PathItem[Params, Responses]

    val f = mockFunction[HttpRequest, ToResponseMarshallable]

    val api = OpenApi[Paths](paths =
      PathItem(path =
        "/app/e1", method = GET, operation = Operation(QueryParameter[Int]('q) :: HNil, ResponseValue[String](200), f)))

    implicit val jsonFormat = apiFormat[Paths]

    val route = RouteGen.openApiRoute(api, includeSwaggerRoute = true)

    val swaggerRequest = get("/swagger.json")

    swaggerRequest ~> route ~> check {
      status shouldBe OK
      responseAs[String] shouldBe api.toJson.prettyPrint
    }
  }

  "host element" should "be included in the route defn" in {
    type Paths = PathItem[HNil, HNil]

    val f = mockFunction[HttpRequest, ToResponseMarshallable]

    val api = OpenApi[Paths](
      host = Some("foo"),
      paths =
        PathItem(path =
          "/app", method = GET, operation = Operation(HNil, HNil, f)))

    implicit val jsonFormat = apiFormat[Paths]

    val route = RouteGen.openApiRoute(api)

    val request0 = get("bar", "/app")
    request0 ~> seal(route) ~> check {
      status shouldBe NotFound
    }

    val request1 = get("foo", "/app")
    f expects request1 returning "x"
    request1 ~> route ~> check {
      status shouldBe OK
    }
  }

  "schemes" should "be included in the route defn" in {
    type Paths = PathItem[HNil, HNil]

    val f = mockFunction[HttpRequest, ToResponseMarshallable]

    val api = OpenApi[Paths](
      schemes = Some(Seq("http")),
      paths =
        PathItem(path =
          "/app", method = GET, operation = Operation(HNil, HNil, f))
    )

    implicit val jsonFormat = apiFormat[Paths]

    val route = RouteGen.openApiRoute(api, includeSwaggerRoute = true)

    val request0 = Get("https://foo.com/app")
    request0 ~> route ~> check {
      inside(rejection) {
        case SchemeRejection(scheme) => scheme shouldBe "http"
      }
    }

    val request1 = Get("http://foo.com/app")
    f expects request1 returning "x"
    request1 ~> route ~> check {
      status shouldBe OK
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
