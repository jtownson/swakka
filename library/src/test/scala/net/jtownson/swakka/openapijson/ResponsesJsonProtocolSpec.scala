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

import net.jtownson.swakka.openapijson.ResponsesJsonProtocol._
import net.jtownson.swakka.openapimodel._

import org.scalatest.FlatSpec
import org.scalatest.Matchers._

import shapeless.HNil

import spray.json.{JsObject, JsString, _}

class ResponsesJsonProtocolSpec extends FlatSpec {

  object UserCode {

    case class Success(id: String)

    case class Error(msg: String)
  }

  import UserCode._

  "Responses JsonProtocol" should "write HNil as empty" in {
    hNilResponseFormat.write(HNil) shouldBe JsObject()
  }

  it should "write a complex response" in {

    val responses =
      ResponseValue[Success, HNil]("200", "ok") ::
        ResponseValue[String, HNil]("404", "not found") ::
        ResponseValue[Error, HNil]("500", "server error") ::
        HNil

    val expectedJson =
      JsObject(
        "200" -> JsObject(
          "description" -> JsString("ok"),
          "schema" ->
            JsObject(
              "type" -> JsString("object"),
              "required" -> JsArray(JsString("id")),
              "properties" -> JsObject(
                "id" -> JsObject(
                  "type" -> JsString("string"))
              )
            )
        ),
        "404" -> JsObject(
          "description" -> JsString("not found"),
          "schema" ->
            JsObject(
              "type" -> JsString("string"))
        ),
        "500" -> JsObject(
          "description" -> JsString("server error"),
          "schema" ->
            JsObject(
              "type" -> JsString("object"),
              "required" -> JsArray(JsString("msg")),
              "properties" -> JsObject(
                "msg" -> JsObject(
                  "type" -> JsString("string"))
              )
            )
        )
      )

    responses.toJson shouldBe expectedJson
  }


  it should "write response headers" in {

    val responses =
      ResponseValue[Success, Header[String]]("200", "ok", Header[String](Symbol("x-foo"), Some("a header")))

    val expectedJson =
      JsObject(
        "200" -> JsObject(
          "description" -> JsString("ok"),
          "headers" -> JsObject(
            "x-foo" -> JsObject(
              "type" -> JsString("string"),
              "description" -> JsString("a header")
            )
          ),
          "schema" ->
            JsObject(
              "type" -> JsString("object"),
              "required" -> JsArray(JsString("id")),
              "properties" -> JsObject(
                "id" -> JsObject(
                  "type" -> JsString("string"))
              )
            )
        )
      )

    responses.toJson shouldBe expectedJson
  }

  it should "implicitly serialize a response with no params or headers" in {

    val responses = ResponseValue[HNil, HNil]("201", "created")
    val expectedJson = JsObject("201" -> JsObject("description" -> JsString("created")))

    responses.toJson shouldBe expectedJson
  }
}
