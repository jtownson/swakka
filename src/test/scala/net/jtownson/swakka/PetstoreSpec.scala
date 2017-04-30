package net.jtownson.swakka

import akka.http.scaladsl.model.HttpMethods.GET
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.testkit.{RouteTest, TestFrameworkInterface}
import net.jtownson.swakka.OpenApiJsonProtocol._
import net.jtownson.swakka.OpenApiModel._
import net.jtownson.swakka.RouteGen.openApiRoute
import net.jtownson.swakka.jsonschema.SchemaWriter._
import net.jtownson.swakka.model.{Info, Licence}
import org.scalamock.scalatest.MockFactory
import org.scalatest.FlatSpec
import org.scalatest.Matchers._
import shapeless.{::, HNil}
import spray.json._

class PetstoreSpec extends FlatSpec with MockFactory with RouteTest with TestFrameworkInterface {

  case class Pet(
                  id: Long,
                  name: String,
                  tag: Option[String] = None)

  type Pets = Seq[Pet]

  implicit val petSchemaWriter = schemaWriter(Pet)

  "Swakka" should "support the petstore example" in {

    type ListPetsParams = QueryParameter[Int] :: HNil
    type ListPetsResponses = ResponseValue[Pets]

    type Paths = PathItem[ListPetsParams, ListPetsResponses]

    val petstoreApi = OpenApi[Paths](
      info = Info(version = "1.0.0", title = "Swagger Petstore", licence = Some(Licence(name = "MIT"))),
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
          parameters =
            QueryParameter[Int](
              name = 'limit,
              description = Some("How many items to return at one time (max 100)"),
              required = true) ::
              HNil,
          responses = ResponseValue[Pets](200, "An paged array of pets"),
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
                "type" -> JsString("integer"),
                "format" -> JsString("int32")
              )
            ),
            "responses" -> JsObject(
              "200" -> JsObject(
                "description" -> JsString("An paged array of pets"),
                "schema" -> JsObject(
                  "type" -> JsString("array"),
                  "items" -> JsObject(
                    "type" -> JsString("object"),
                    "properties" -> JsObject(
                      "id" -> JsObject(
                        "type" -> JsString("integer"),
                        "format" -> JsString("int64")),
                      "name" -> JsObject(
                        "type" -> JsString("string")),
                      "tag" -> JsObject(
                        "type" -> JsString("string"))
                    )
                  )
                )
              )
            )
          )
        )
      )
    )

    Get("http://petstore.swagger.io/v1/swagger.json") ~> apiRoutes ~> check {
      println(responseAs[String])
            responseAs[String] shouldBe expectedJson.prettyPrint
    }
  }

  private def get(path: String): HttpRequest = {
    Get(s"http://petstore.swagger.io$path")
  }

  override def failTest(msg: String): Nothing = throw new AssertionError(msg)
}
