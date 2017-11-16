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

package net.jtownson.swakka.jsonprotocol

import akka.http.scaladsl.model.HttpMethods.{GET, POST}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import net.jtownson.swakka.OpenApiModel._
import net.jtownson.swakka.OpenApiJsonProtocol._
import net.jtownson.swakka.model.Parameters.QueryParameter
import net.jtownson.swakka.model.Responses.ResponseValue
import net.jtownson.swakka.model.SecurityDefinitions.SecurityRequirement
import org.scalatest.FlatSpec
import org.scalatest.Matchers._
import shapeless.{::, HNil}
import spray.json.{JsArray, JsObject, JsString, _}

class PathsJsonProtocolSpec extends FlatSpec {

  "JsonProtocol" should "write a parameterless pathitem" in {

    type Responses = ResponseValue[String, HNil]

    val pathItem = PathItem(
      path = "/ruok",
      method = POST,
      operation = Operation[HNil, () => Route, ResponseValue[String, HNil]](
        parameters = HNil,
        responses = ResponseValue("200", "ok"),
        endpointImplementation = () => complete("dummy")))

    val expectedSwagger = JsObject(
      "/ruok" -> JsObject(
        "post" -> JsObject(
          "responses" -> JsObject(
            "200" -> JsObject(
              "description" -> JsString("ok"),
              "schema" -> JsObject(
                "type" -> JsString("string")
              )
            )
          )
        )
      )
    )

    pathItem.toJson shouldBe expectedSwagger
  }

  it should "write a responseless pathItem as an empty object" in {

    val pathItem = PathItem(
      path = "/ruok",
      method = GET,
      operation = Operation(
        parameters = HNil: HNil,
        responses = HNil: HNil,
        endpointImplementation = () => complete("dummy")))

    val expectedSwagger = JsObject(
      "/ruok" -> JsObject(
        "get" -> JsObject()
      )
    )

    pathItem.toJson shouldBe expectedSwagger
  }

  it should "write an pathItem with a parameter" in {

    type Params = QueryParameter[String] :: HNil
    type Responses = ResponseValue[String, HNil]
    type Paths = PathItem[Params, String => Route, Responses]

    val pathItem: PathItem[Params, String => Route, Responses] = PathItem(
      path = "/ruok",
      method = GET,
      operation = Operation(
        parameters = QueryParameter[String]('q) :: HNil,
        responses = ResponseValue[String, HNil]("200", "ok"),
        endpointImplementation = (_: String) => complete("dummy")))

    val expectedSwagger = JsObject(
      "/ruok" -> JsObject(
        "get" -> JsObject(
          "parameters" ->
            JsArray(
              JsObject(
                "name" -> JsString("q"),
                "in" -> JsString("query"),
                "required" -> JsTrue,
                "type" -> JsString("string")
              )),
          "responses" -> JsObject(
            "200" -> JsObject(
              "description" -> JsString("ok"),
              "schema" -> JsObject(
                "type" -> JsString("string")
              )
            )
          )
        )
      )
    )

    pathItem.toJson shouldBe expectedSwagger
  }

  type OneIntParam = QueryParameter[Int] :: HNil
  type OneStrParam = QueryParameter[String] :: HNil
  type StringResponse = ResponseValue[String, HNil]
  type Paths =
    PathItem[OneIntParam, Int => Route, StringResponse] ::
    PathItem[OneStrParam, String => Route, StringResponse] :: HNil

  it should "write a simple swagger definition" in {
    val api: OpenApi[Paths, HNil] =
      OpenApi(paths =
        PathItem[OneIntParam, Int => Route, StringResponse](
          path = "/app/e1",
          method = GET,
          operation = Operation(
            parameters = QueryParameter[Int]('q) :: HNil,
            responses = ResponseValue[String, HNil]("200", "ok"),
            endpointImplementation = (_: Int) => complete("dummy")
          )
        )
          ::
          PathItem[OneStrParam, String => Route, StringResponse](
            path = "/app/e2",
            method = GET,
            operation = Operation(
              parameters = QueryParameter[String]('q) :: HNil,
              responses = ResponseValue[String, HNil]("200", "ok"),
              endpointImplementation = (_: String) => complete("dummy")
            )
          )
          :: HNil
      )

    val expectedJson = JsObject(
      "swagger" -> JsString("2.0"),
      "info" -> JsObject(
        "title" -> JsString(""),
        "version" -> JsString("")
      ),
      "paths" -> JsObject(
        "/app/e1" -> JsObject(
          "get" -> JsObject(
            "parameters" -> JsArray(
              JsObject(
                "name" -> JsString("q"),
                "in" -> JsString("query"),
                "required" -> JsTrue,
                "type" -> JsString("integer"),
                "format" -> JsString("int32")
              )),
            "responses" -> JsObject(
              "200" -> JsObject(
                "description" -> JsString("ok"),
                "schema" -> JsObject(
                  "type" -> JsString("string")
                )
              )
            )
          )
        ),
        "/app/e2" -> JsObject(
          "get" -> JsObject(
            "parameters" -> JsArray(
              JsObject(
                "name" -> JsString("q"),
                "in" -> JsString("query"),
                "required" -> JsTrue,
                "type" -> JsString("string")
              )),
            "responses" -> JsObject(
              "200" -> JsObject(
                "description" -> JsString("ok"),
                "schema" -> JsObject(
                  "type" -> JsString("string")
                )
              )
            )
          )
        )
      )
    )

    apiFormat[Paths, HNil].write(api) shouldBe expectedJson
  }

  it should "write an empty swagger definition" in {
    val api = OpenApi[HNil, HNil](paths = HNil)
    val expectedJson = JsObject(
      "swagger" -> JsString("2.0"),
      "info" -> JsObject(
        "title" -> JsString(""),
        "version" -> JsString("")
      ),
      "paths" -> JsObject()
    )

    apiFormat[HNil, HNil].write(api) shouldBe expectedJson
  }

  it should "write a swagger security definition with a security requirement" in {

    val api = OpenApi(
      paths =
        PathItem(
          path = "/app/e1",
          method = GET,
          operation = Operation(
            parameters = QueryParameter[Int]('q) :: HNil,
            responses = ResponseValue[String, HNil]("200", "ok"),
            security = Some(Seq(SecurityRequirement('auth, Seq("grant1", "grant2")))),
            endpointImplementation = (_: Int) => complete("dummy")
          )
        ) :: HNil
      )

    val expectedJson = JsObject(
      "swagger" -> JsString("2.0"),
      "info" -> JsObject(
        "title" -> JsString(""),
        "version" -> JsString("")
      ),
      "paths" -> JsObject(
        "/app/e1" -> JsObject(
          "get" -> JsObject(
            "parameters" -> JsArray(
              JsObject(
                "name" -> JsString("q"),
                "in" -> JsString("query"),
                "required" -> JsTrue,
                "type" -> JsString("integer"),
                "format" -> JsString("int32")
              )),
            "responses" -> JsObject(
              "200" -> JsObject(
                "description" -> JsString("ok"),
                "schema" -> JsObject(
                  "type" -> JsString("string")
                )
              )
            ),
            "security" -> JsArray(
              JsObject(
                "auth" -> JsArray(JsString("grant1"), JsString("grant2")
                )
              )
            )
          )
        )
      )
    )

    api.toJson shouldBe expectedJson
  }

  it should "combine path items where the path is equal" in {

    type Paths =
      PathItem[HNil, () => Route, HNil] ::
      PathItem[HNil, () => Route, HNil] :: HNil

    val paths: Paths =
      PathItem[HNil, () => Route, HNil](
        path = "/app",
        method = GET,
        operation = Operation(
          endpointImplementation = () => complete("dummy")
        )
      ) ::
        PathItem[HNil, () => Route, HNil](
          path = "/app",
          method = POST,
          operation = Operation(
            endpointImplementation = () => complete("dummy")
          )
        ) ::
        HNil

    val expectedJson =
      JsObject(
        "/app" -> JsObject(
          "get" -> JsObject(),
          "post" -> JsObject()
        )
      )

    paths.toJson shouldBe expectedJson
  }
}
