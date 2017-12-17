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

package net.jtownson.swakka.openapijson2

import net.jtownson.swakka.openapijson2.OpenApiJsonProtocol2._
import net.jtownson.swakka.openapimodel._
import org.scalatest.FlatSpec
import org.scalatest.Matchers._
import shapeless.HNil
import spray.json.{JsObject, JsString, _}
import shapeless.syntax.singleton._

class ResponsesJsonProtocol2Spec extends FlatSpec {

  object UserCode {

    case class Success(id: String)

    case class Error(msg: String)
  }

  import UserCode._

  "Responses JsonProtocol" should "write HNil as empty" in {
    hNilResponseFormat.write(HNil) shouldBe JsObject()
  }

  val expectedMultiResponseJson =
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

//  it should "write responses given as a LabelledGeneric case class" in {
//    case class Responses(
//                          `200`: ResponseValue[Success, HNil],
//                          `404`: ResponseValue[String, HNil],
//                          `500`: ResponseValue[Error, HNil])
//
//    val responses = Responses(
//      ResponseValue[Success]("foo", "ok"),
//      ResponseValue[String]("bar", "not found"),
//      ResponseValue[Error]("baz", "server error")
//    )
//
//    responses.toJson shouldBe expectedMultiResponseJson
//  }
//
  it should "write responses given as a LabelledGeneric Record" in {

    val responses =
      "200" ->> ResponseValue[Success]("foo", "ok") ::
//      "404" ->> ResponseValue[String]("bar", "not found") ::
//      "500" ->> ResponseValue[Error]("baz", "server error") ::
      HNil

//    implicitly[ResponseJsonFormat[ResponseValue[Success, HNil]]]
//
//    implicitly[ResponseJsonFormat[ResponseValue[Success, HNil] :: ResponseValue[Error, HNil] :: HNil]]

    responses.toJson shouldBe expectedMultiResponseJson
  }

//  it should "write a complex response" in {
//
//    val responses =
//      ResponseValue[Success]("200", "ok") ::
//        ResponseValue[String]("404", "not found") ::
//        ResponseValue[Error]("500", "server error") ::
//        HNil
//
//    responses.toJson shouldBe expectedMultiResponseJson
//  }
//
//
//  it should "write response headers" in {
//
//    val responses =
//      ResponseValue[Success, Header[String]]("200", "ok", Header[String](Symbol("x-foo"), Some("a header")))
//
//    val expectedJson =
//      JsObject(
//        "200" -> JsObject(
//          "description" -> JsString("ok"),
//          "headers" -> JsObject(
//            "x-foo" -> JsObject(
//              "type" -> JsString("string"),
//              "description" -> JsString("a header")
//            )
//          ),
//          "schema" ->
//            JsObject(
//              "type" -> JsString("object"),
//              "required" -> JsArray(JsString("id")),
//              "properties" -> JsObject(
//                "id" -> JsObject(
//                  "type" -> JsString("string"))
//              )
//            )
//        )
//      )
//
//    responses.toJson shouldBe expectedJson
//  }
//
//  it should "implicitly serialize a response with no params or headers" in {
//
//    val responses = ResponseValue[HNil]("201", "created")
//    val expectedJson = JsObject("201" -> JsObject("description" -> JsString("created")))
//
//    responses.toJson shouldBe expectedJson
//  }
}
