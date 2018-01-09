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

import org.scalatest.FlatSpec
import org.scalatest.Matchers._
import spray.json._
import akka.http.scaladsl.model.DateTime
import net.jtownson.swakka.openapimodel._

class HeadersJsonProtocolSpec extends FlatSpec {

  "HeadersJsonProtocol" should "serialize a string" in {

    val expectedJson = JsObject(
      "x-foo" -> JsObject(
        "type" -> JsString("string"),
        "description" -> JsString("some info")
      ))

    Header[String](Symbol("x-foo"), Some("some info")).toJson shouldBe expectedJson
  }

  it should "serialize a Double" in {

    val expectedJson = JsObject(
      "x-foo" -> JsObject(
        "type" -> JsString("number"),
        "format" -> JsString("double"),
        "description" -> JsString("some info")
      ))

    Header[Double](Symbol("x-foo"), Some("some info")).toJson shouldBe expectedJson
  }

  it should "serialize a Float" in {

    val expectedJson = JsObject(
      "x-foo" -> JsObject(
        "type" -> JsString("number"),
        "format" -> JsString("float"),
        "description" -> JsString("some info")
      ))

    Header[Float](Symbol("x-foo"), Some("some info")).toJson shouldBe expectedJson
  }

  it should "serialize an Int" in {

    val expectedJson = JsObject(
      "x-foo" -> JsObject(
        "type" -> JsString("integer"),
        "format" -> JsString("int32"),
        "description" -> JsString("some info")
      ))

    Header[Int](Symbol("x-foo"), Some("some info")).toJson shouldBe expectedJson
  }

  it should "serialize a Long" in {

    val expectedJson = JsObject(
      "x-foo" -> JsObject(
        "type" -> JsString("integer"),
        "format" -> JsString("int64"),
        "description" -> JsString("some info")
      ))

    Header[Long](Symbol("x-foo"), Some("some info")).toJson shouldBe expectedJson
  }

  it should "serialize a Boolean" in {

    val expectedJson = JsObject(
      "x-foo" -> JsObject(
        "type" -> JsString("boolean"),
        "description" -> JsString("some info")
      ))

    Header[Boolean](Symbol("x-foo"), Some("some info")).toJson shouldBe expectedJson
  }

  it should "serialize a DateTime" in {
    val expectedJson = JsObject(
      "X-Expires-After" -> JsObject(
      "type" -> JsString("string"),
      "format" -> JsString("date-time"),
      "description" -> JsString("date in UTC when token expires")
    ))

    Header[DateTime](Symbol("X-Expires-After"), Some("date in UTC when token expires")).toJson shouldBe expectedJson
  }

  type Headers = (Header[String], Header[Long])

  val tupleHeaderFormat = implicitly[HeadersJsonFormat[Headers]]

  // implicit JsonFormat[Headers] required by toJson has
  // an ambiguity with a tuple format in spray json.
  it should "serialize a combination of headers" in {

    val headers: Headers = (
      Header[String](Symbol("x-s"), Some("a string header")),
      Header[Long](Symbol("x-i"), Some("a long header")))

    val expectedJson = JsObject(
      "x-s" -> JsObject(
        "type" -> JsString("string"),
        "description" -> JsString("a string header")
      ),
      "x-i" -> JsObject(
        "type" -> JsString("integer"),
        "format" -> JsString("int64"),
        "description" -> JsString("a long header")
      )
    )

    tupleHeaderFormat.write(headers) shouldBe expectedJson
  }

}
