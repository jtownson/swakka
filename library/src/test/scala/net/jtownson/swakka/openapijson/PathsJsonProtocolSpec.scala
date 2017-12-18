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

package net.jtownson.swakka.openapijson

import akka.http.scaladsl.model.HttpMethods.{GET, POST}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import spray.json.{JsArray, JsObject, JsString, _}

import net.jtownson.swakka.openapimodel._
import net.jtownson.swakka.openapijson.PathsJsonProtocol._

import org.scalatest.FlatSpec
import org.scalatest.Matchers._

import shapeless.HNil

class PathsJsonProtocolSpec extends FlatSpec {

  val dummyEndpoint: () => Route =
    () => complete("dummy")

  val dummyStringEndpoint: String => Route =
    _ => complete("dummy")

  val dummyIntEndpoint: Int => Route =
    _ => complete("dummy")


  "JsonProtocol" should "write a parameterless pathitem" in {

    val pathItem: PathItem[HNil, () => Route, ResponseValue[String, HNil]] = PathItem(
      path = "/ruok",
      method = POST,
      operation = Operation(
        responses = ResponseValue[String]("200", "ok"),
        endpointImplementation = dummyEndpoint))

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
        endpointImplementation = dummyEndpoint))

    val expectedSwagger = JsObject(
      "/ruok" -> JsObject(
        "get" -> JsObject()
      )
    )

    pathItem.toJson shouldBe expectedSwagger
  }

  it should "write an pathItem with a parameter" in {

    val pathItem = PathItem(
      path = "/ruok",
      method = GET,
      operation = Operation(
        parameters = QueryParameter[String]('q) :: HNil,
        responses = ResponseValue[String]("200", "ok"),
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

  it should "combine path items where the path is equal" in {

    val paths =
      PathItem(
        path = "/app",
        method = GET,
        operation = Operation(
          endpointImplementation = dummyEndpoint
        )
      ) ::
        PathItem(
          path = "/app",
          method = POST,
          operation = Operation(
            endpointImplementation = dummyEndpoint
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

  it should "write the deprecate flag" in {
    val paths =
      PathItem(
        path = "/app",
        method = GET,
        operation = Operation(
          deprecated = true,
          endpointImplementation = dummyEndpoint
        )
      )

    val expectedJson =
      JsObject(
        "/app" -> JsObject(
          "get" -> JsObject(
            "deprecated" -> JsBoolean(true)
          )
        )
      )

    paths.toJson shouldBe expectedJson
  }
}
