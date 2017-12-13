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
import spray.json._

import net.jtownson.swakka.openapimodel._
import net.jtownson.swakka.openapijson._
import net.jtownson.swakka.coreroutegen._
import net.jtownson.swakka.openapiroutegen._

import org.scalamock.scalatest.MockFactory
import org.scalatest.FlatSpec
import org.scalatest.Matchers._

import shapeless.{::, HNil}


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


  "Swakka" should "support the petstore example" in {

    val dummyRoute: Route = complete("dummy")

    val petstoreApi = OpenApi(
      info = Info(version = "1.0.0", title = "Swagger Petstore", licence = Some(License(name = "MIT"))),
      host = Some("petstore.swagger.io"),
      basePath = Some("/v1"),
      schemes = Some(Seq("http")),
      consumes = Some(Seq("application/json")),
      produces = Some(Seq("application/json")),
      paths =
        PathItem(
          path = "/pets",
          method = GET,
          operation = Operation(
            summary = Some("List all pets"),
            operationId = Some("listPets"),
            tags = Some(Seq("pets")),
            parameters = Tuple1(
              QueryParameter[Int](
                name = 'limit,
                description = Some("How many items to return at one time (max 100)"))),
            responses =
              ResponseValue[Pets, Header[String]](
                responseCode = "200",
                description = "An paged array of pets",
                headers = Header[String](Symbol("x-next"), Some("A link to the next page of responses"))) ::
                ResponseValue[Error, HNil](
                  responseCode = "default",
                  description = "unexpected error"
                ) :: HNil,
            endpointImplementation = (_: Int) => dummyRoute)) ::
          PathItem(
            path = "/pets",
            method = POST,
            operation = Operation(
              summary = Some("Create a pet"),
              operationId = Some("createPets"),
              tags = Some(Seq("pets")),
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
              endpointImplementation = () => dummyRoute
            )
          ) ::
          PathItem(
            path = "/pets/{petId}",
            method = GET,
            operation = Operation(
              summary = Some("Info for a specific pet"),
              operationId = Some("showPetById"),
              tags = Some(Seq("pets")),
              parameters = Tuple1(
                PathParameter[String]('petId, Some("The id of the pet to retrieve"))),
              responses =
                ResponseValue[Pets, HNil]("200", "Expected response to a valid request") ::
                ResponseValue[Error, HNil]("default", "unexpected error") ::
                HNil,
              endpointImplementation = (_: String) => dummyRoute
            )
          ) ::
          HNil
    )

    val apiRoutes = openApiRoute(petstoreApi, Some(DocRouteSettings()))

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
