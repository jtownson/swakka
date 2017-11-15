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

import akka.http.scaladsl.model.HttpMethods.{GET, POST}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.{RouteTest, TestFrameworkInterface}
import net.jtownson.swakka.OpenApiModel._
import net.jtownson.swakka.OpenApiJsonProtocol._
import net.jtownson.swakka.RouteGen.openApiRoute
import net.jtownson.swakka.jsonschema.SchemaWriter._
import net.jtownson.swakka.model.Parameters.{PathParameter, QueryParameter}
import net.jtownson.swakka.model.Responses.{Header, ResponseValue}
import net.jtownson.swakka.model.{Info, License}
import net.jtownson.swakka.routegen.CorsUseCases.NoCors
import net.jtownson.swakka.routegen.{CorsUseCases, SwaggerRouteSettings}
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

    type ListPetsEndpoint = Int => Route
    type ListPetsParams = QueryParameter[Int] :: HNil
    type ListPetsResponses = ResponseValue[Pets, Header[String]] :: ResponseValue[Error, HNil] :: HNil

    type CreatePetEndpoint = () => Route
    type CreatePetParams = HNil
    type CreatePetResponses = ResponseValue[HNil, HNil] :: ResponseValue[Error, HNil] :: HNil

    type ShowPetEndpoint = String => Route
    type ShowPetParams = PathParameter[String] :: HNil
    type ShowPetResponses = ResponseValue[Pets, HNil] :: ResponseValue[Error, HNil] :: HNil

    type Paths =
      PathItem[ListPetsEndpoint, ListPetsParams, ListPetsResponses] ::
      PathItem[CreatePetEndpoint, HNil, CreatePetResponses] ::
      PathItem[ShowPetEndpoint, ShowPetParams, ShowPetResponses] :: HNil


    val petstoreApi = OpenApi[Paths, HNil](
      info = Info(version = "1.0.0", title = "Swagger Petstore", licence = Some(License(name = "MIT"))),
      host = Some("petstore.swagger.io"),
      basePath = Some("/v1"),
      schemes = Some(Seq("http")),
      consumes = Some(Seq("application/json")),
      produces = Some(Seq("application/json")),
      paths =
        PathItem[ListPetsEndpoint, ListPetsParams, ListPetsResponses](
          path = "/pets",
          method = GET,
          operation = Operation(
            summary = Some("List all pets"),
            operationId = Some("listPets"),
            tags = Some(Seq("pets")),
            parameters =
              QueryParameter[Int](
                name = 'limit,
                description = Some("How many items to return at one time (max 100)")) ::
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
            endpointImplementation = (_: Int) => complete("dummy"))) ::
          PathItem[CreatePetEndpoint, CreatePetParams, CreatePetResponses](
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
              endpointImplementation = () => complete("dummy")
            )
          ) ::
          PathItem[ShowPetEndpoint, ShowPetParams, ShowPetResponses](
            path = "/pets/{petId}",
            method = GET,
            operation = Operation(
              summary = Some("Info for a specific pet"),
              operationId = Some("showPetById"),
              tags = Some(Seq("pets")),
              parameters =
                PathParameter[String]('petId, Some("The id of the pet to retrieve")) ::
                  HNil,
              responses =
                ResponseValue[Pets, HNil]("200", "Expected response to a valid request") ::
                ResponseValue[Error, HNil]("default", "unexpected error") ::
                HNil,
              endpointImplementation = (_: String) => complete("dummy")
            )
          ) ::
          HNil
    )

    val apiRoutes = openApiRoute(petstoreApi, Some(SwaggerRouteSettings()))

    val expectedJson = JsObject(
      "swagger" -> JsString("2.0"),
      "info" -> JsObject(
        "title" -> JsString("Swagger Petstore"),
        "version" -> JsString("1.0.0"),
        "license" -> JsObject(
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
                "required" -> JsBoolean(true),
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
        ),
        "/pets/{petId}" -> JsObject(
          "get" -> JsObject(
            "summary" -> JsString("Info for a specific pet"),
            "operationId" -> JsString("showPetById"),
            "tags" -> JsArray(
              JsString("pets")
            ),
            "parameters" -> JsArray(
              JsObject(
                "name" -> JsString("petId"),
                "in" -> JsString("path"),
                "required" -> JsBoolean(true),
                "description" -> JsString("The id of the pet to retrieve"),
                "type" -> JsString("string")
              )
            ),
            "responses" -> JsObject(
              "200" -> JsObject(
                "description" -> JsString("Expected response to a valid request"),
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
          )
        )
      )
    )

    Get("http://petstore.swagger.io/v1/swagger.json") ~> apiRoutes ~> check {
      JsonParser(responseAs[String]) shouldBe expectedJson
    }
  }

  override def failTest(msg: String): Nothing = throw new AssertionError(msg)
}
