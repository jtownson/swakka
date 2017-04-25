package net.jtownson.swakka

import akka.http.scaladsl.model.HttpMethods.GET
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.testkit.{RouteTest, TestFrameworkInterface}
import net.jtownson.swakka.OpenApiModel._
import net.jtownson.swakka.RouteGen.openApiRoute
import net.jtownson.swakka.model.{Info, Licence}
import org.scalamock.scalatest.MockFactory
import org.scalatest.FlatSpec
import org.scalatest.Matchers._
import shapeless.{::, HNil}
import spray.json.{JsArray, JsBoolean, JsObject, JsString}

class PetstoreSpec extends FlatSpec with MockFactory with RouteTest with TestFrameworkInterface {

  case class Pet(id: Int, name: String, tag: Option[String] = None)

  type Pets = Seq[Pet]

  val apiInfo = Info(version = "1.0.0", title = "Swagger Petstore", licence = Some(Licence(name = "MIT")))

  import OpenApiJsonProtocol._

  "Swakka" should "support the petstore example" in {

    type ListPetsParams = QueryParameter[Int] :: HNil
    type ListPetsResponses = HNil

    type Paths = PathItem[ListPetsParams, ListPetsResponses]

    val petstoreApi = OpenApi[Paths](
      info = apiInfo,
      host = Some("petstore.swagger.io"),
      basePath = Some("/v1"),
      schemes = Some(Seq("http")),
      consumes = Some(Seq("application/json")),
      produces = Some(Seq("application/json")),
      paths = PathItem(
        path = "/pets",
        method = GET,
        operation = Operation(
          summary = Some("List all pets"),
          operationId = Some("listPets"),
          tags = Some(Seq("pets")),
          parameters = QueryParameter[Int](
            'limit,
            Some("How many items to return at one time (max 100)"),
            required = true) :: HNil,
          responses = HNil,
          endpointImplementation = _ => ???)))

    val apiRoutes = openApiRoute(petstoreApi, includeSwaggerRoute = true)

    val expectedJson = JsObject(
      "swagger" -> JsString("2.0"),
      "info" -> JsObject(
        "title" -> JsString("Swagger Petstore"),
        "version" -> JsString("1.0.0"),
        "licence" -> JsObject(
          "name" -> JsString("MIT")
        )
      ),
      "host" -> JsString("petstore.swagger.io"),
      "basePath" -> JsString("/v1"),
      "schemes" -> JsArray(JsString("http")),
      "consumes" -> JsArray(JsString("application/json")),
      "produces" -> JsArray(JsString("application/json")),
      "paths" -> JsObject(
        "/pets" -> JsObject(
          "get" -> JsObject(
            "summary" -> JsString("List all pets"),
            "operationId" -> JsString("listPets"),
            "tags" -> JsArray(JsString("pets")),
            "parameters" -> JsArray(
              JsObject(
                "name" -> JsString("limit"),
                "in" -> JsString("query"),
                "description" -> JsString("How many items to return at one time (max 100)"),
                "required" -> JsBoolean(true),
                "type" -> JsString("integer")
              )
            )
          )
        )
      )
    )
    /*
            "parameters": [
              {
                "name": "limit",
                "in": "query",
                "description": "How many items to return at one time (max 100)",
                "required": false,
                "type": "integer",
                "format": "int32"
              }
            ],
     */
    Get("http://petstore.swagger.io/v1/swagger.json") ~> apiRoutes ~> check {
      responseAs[String] shouldBe expectedJson.prettyPrint
    }
  }

  private def get(path: String): HttpRequest = {
    Get(s"http://petstore.swagger.io$path")
  }

  override def failTest(msg: String): Nothing = throw new AssertionError(msg)
}
