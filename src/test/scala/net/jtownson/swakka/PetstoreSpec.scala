package net.jtownson.swakka

import akka.http.scaladsl.model.HttpMethods.{GET, POST}
import akka.http.scaladsl.testkit.{RouteTest, TestFrameworkInterface}
import net.jtownson.swakka.OpenApiModel._
import net.jtownson.swakka.OpenApiJsonProtocol._
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

  case class Error(
                    id: Int,
                    message: String
                  )

  implicit val petSchemaWriter = schemaWriter(Pet)
  implicit val errorSchemaWriter = schemaWriter(Error)

  "Swakka" should "support the petstore example" in {

    type ListPetsParams = QueryParameter[Int] :: HNil
    type ListPetsResponses = ResponseValue[Pets, Header[String]] :: ResponseValue[Error, HNil] :: HNil

    type CreatePetParams = HNil
    type CreatePetResponses = ResponseValue[HNil, HNil] :: ResponseValue[Error, HNil] :: HNil

    type Paths = PathItem[ListPetsParams, ListPetsResponses] :: PathItem[HNil, CreatePetResponses] :: HNil


    val petstoreApi = OpenApi[Paths](
      info = Info(version = "1.0.0", title = "Swagger Petstore", licence = Some(Licence(name = "MIT"))),
      host = Some("petstore.swagger.io"),
      basePath = Some("/v1"),
      schemes = Some(Seq("http")),
      consumes = Some(Seq("application/json")),
      produces = Some(Seq("application/json")),
      paths =
        PathItem[ListPetsParams, ListPetsResponses](
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
                required = false) ::
                HNil,
            responses =
              ResponseValue[Pets, Header[String]](
                responseCode = "200",
                description = "An paged array of pets",
                headers = Header[String](Symbol("x-next"), Some("A link to the next page of responses"))) ::
                ResponseValue[Error, HNil](
                  responseCode = "default",
                  description = "unexpected error"
                ) :: HNil,
            endpointImplementation = _ => ???)) ::
          PathItem[CreatePetParams, CreatePetResponses](
            path = "/pets",
            method = POST,
            operation = Operation(
              summary = Some("Create a pet"),
              operationId = Some("createPets"),
              tags = Some(Seq("pets")),
              parameters = HNil,
              responses =
                ResponseValue[HNil, HNil](
                  responseCode = "201",
                  description = "Null response"
                ) ::
                ResponseValue[Error, HNil](
                  responseCode = "default",
                  description = "unexpected error"
                ) ::
                HNil,
              endpointImplementation = _ => ???
            )
          ) ::
          HNil
    )

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
            "tags" -> JsArray(
              JsString("pets")
            ),
            "parameters" -> JsArray(
              JsObject(
                "name" -> JsString("limit"),
                "in" -> JsString("query"),
                "description" -> JsString("How many items to return at one time (max 100)"),
                "required" -> JsBoolean(false),
                "type" -> JsString("integer"),
                "format" -> JsString("int32")
              )
            ),
            "responses" -> JsObject(
              "200" -> JsObject(
                "description" -> JsString("An paged array of pets"),
                "headers" -> JsObject(
                  "x-next" -> JsObject(
                    "type" -> JsString("string"),
                    "description" -> JsString("A link to the next page of responses")
                  )
                ),
                "schema" -> JsObject(
                  "type" -> JsString("array"),
                  "items" -> JsObject(
                    "type" -> JsString("object"),
                    "required" -> JsArray(JsString("id"), JsString("name")),
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
              ),
              "default" -> JsObject(
                "description" -> JsString("unexpected error"),
                "schema" -> JsObject(
                  "type" -> JsString("object"),
                  "required" -> JsArray(JsString("id"), JsString("message")),
                  "properties" -> JsObject(
                    "id" -> JsObject(
                      "type" -> JsString("integer"),
                      "format" -> JsString("int32")
                    ),
                    "message" -> JsObject(
                      "type" -> JsString("string")
                    )
                  )
                )
              )
            )
          ),
          "post" -> JsObject(
            "summary" -> JsString("Create a pet"),
            "operationId" -> JsString("createPets"),
            "tags" -> JsArray(JsString("pets")),
            "responses" -> JsObject(
              "201" -> JsObject(
                "description" -> JsString("Null response")
              ),
              "default" -> JsObject(
                "description" -> JsString("unexpected error"),
                "schema" -> JsObject(
                  "type" -> JsString("object"),
                  "required" -> JsArray(JsString("id"), JsString("message")),
                  "properties" -> JsObject(
                    "id" -> JsObject(
                      "type" -> JsString("integer"),
                      "format" -> JsString("int32")
                    ),
                    "message" -> JsObject(
                      "type" -> JsString("string")
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
//      println(responseAs[String])
      JsonParser(responseAs[String]) shouldBe expectedJson
    }
  }

  override def failTest(msg: String): Nothing = throw new AssertionError(msg)
}
