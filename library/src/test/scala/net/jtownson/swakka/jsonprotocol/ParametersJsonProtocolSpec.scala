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

import akka.http.scaladsl.server.directives.FileInfo
import akka.stream.scaladsl.Source
import akka.util.ByteString
import net.jtownson.swakka.jsonprotocol.ParametersJsonProtocol._
import net.jtownson.swakka.jsonschema.SchemaWriter._
import net.jtownson.swakka.misc.jsObject
import net.jtownson.swakka.model.Parameters._
import org.scalatest.Matchers._
import org.scalatest._
import shapeless.{::, HNil}
import spray.json.{JsArray, JsBoolean, JsObject, JsString, JsValue, _}
import org.scalatest.prop.TableDrivenPropertyChecks._

class ParametersJsonProtocolSpec extends FlatSpec {

  object UserStuff {

    case class Pet(petName: String)

    val defaultPet = Pet("I'm a default. Boo!")

    implicit val petJsonWriter = jsonFormat1(Pet)

    object Statuses extends Enumeration {
      val placed, approved, delivered = Value
    }
  }

  import UserStuff._

  val parameters = Table(
    ("testcase", "parameter", "expected json"),

    // Required query parameter types
    ("Required string query",
      QueryParameter[String]('qp, Some("a description")).toJson,
      queryParamJson(true, "string")),

    ("Required boolean query",
      QueryParameter[Boolean]('qp, Some("a description")).toJson,
      queryParamJson(true, "boolean")),

    ("Required int query",
      QueryParameter[Int]('qp, Some("a description")).toJson,
      queryParamJson(true, "integer", Some("int32"))),

    ("Required long query",
      QueryParameter[Long]('qp, Some("a description")).toJson,
      queryParamJson(true, "integer", Some("int64"))),

    ("Required float query",
      QueryParameter[Float]('qp, Some("a description")).toJson,
      queryParamJson(true, "number", Some("float"))),

    ("Required double query",
      QueryParameter[Double]('qp, Some("a description")).toJson,
      queryParamJson(true, "number", Some("double"))),

    // (Required) path parameter types
    ("Required string path",
      PathParameter[String]('petId, Some("a description")).toJson,
      pathParameterJson(true, "string")),

    ("Required boolean path",
      PathParameter[Boolean]('petId, Some("a description")).toJson,
      pathParameterJson(true, "boolean")),

    ("Required int path",
      PathParameter[Int]('petId, Some("a description")).toJson,
      pathParameterJson(true, "integer", Some("int32"))),

    ("Required long path",
      PathParameter[Long]('petId, Some("a description")).toJson,
      pathParameterJson(true, "integer", Some("int64"))),

    ("Required float path",
      PathParameter[Float]('petId, Some("a description")).toJson,
      pathParameterJson(true, "number", Some("float"))),

    ("Required double path",
      PathParameter[Double]('petId, Some("a description")).toJson,
      pathParameterJson(true, "number", Some("double"))),

    // Required header parameter types
    ("Required string header",
      HeaderParameter[String](Symbol("x-my-header"), Some("a header")).toJson,
      headerParamJson(true, "string")),

    ("Required boolean header",
      HeaderParameter[Boolean](Symbol("x-my-header"), Some("a header")).toJson,
      headerParamJson(true, "boolean")),

    ("Required int header",
      HeaderParameter[Int](Symbol("x-my-header"), Some("a header")).toJson,
      headerParamJson(true, "integer", Some("int32"))),

    ("Required long header",
      HeaderParameter[Long](Symbol("x-my-header"), Some("a header")).toJson,
      headerParamJson(true, "integer", Some("int64"))),

    ("Required float header",
      HeaderParameter[Float](Symbol("x-my-header"), Some("a header")).toJson,
      headerParamJson(true, "number", Some("float"))),

    ("Required double header",
      HeaderParameter[Double](Symbol("x-my-header"), Some("a header")).toJson,
      headerParamJson(true, "number", Some("double"))),

    // Required form parameter types
    ("Required string form param",
      FormFieldParameter[String]('f, Some("a form field")).toJson,
      formParamJson(true, "string")),

    ("Required boolean form param",
      FormFieldParameter[Boolean]('f, Some("a form field")).toJson,
      formParamJson(true, "boolean")),

    ("Required int form param",
      FormFieldParameter[Int]('f, Some("a form field")).toJson,
      formParamJson(true, "integer", Some("int32"))),

    ("Required long form param",
      FormFieldParameter[Long]('f, Some("a form field")).toJson,
      formParamJson(true, "integer", Some("int64"))),

    ("Required float form param",
      FormFieldParameter[Float]('f, Some("a form field")).toJson,
      formParamJson(true, "number", Some("float"))),

    ("Required double form param",
      FormFieldParameter[Double]('f, Some("a form field")).toJson,
      formParamJson(true, "number", Some("double"))),

    ("Required file form param",
      FormFieldParameter[(FileInfo, Source[ByteString, Any])]('f, Some("a form field")).toJson,
      formParamJson(true, "file")),

    // Optional form parameter types
    ("Optional string form param",
      FormFieldParameter[Option[String]]('f, Some("a form field")).toJson,
      formParamJson(false, "string")),

    ("Optional boolean form param",
      FormFieldParameter[Option[Boolean]]('f, Some("a form field")).toJson,
      formParamJson(false, "boolean")),

    ("Optional int form param",
      FormFieldParameter[Option[Int]]('f, Some("a form field")).toJson,
      formParamJson(false, "integer", Some("int32"))),

    ("Optional long form param",
      FormFieldParameter[Option[Long]]('f, Some("a form field")).toJson,
      formParamJson(false, "integer", Some("int64"))),

    ("Optional float form param",
      FormFieldParameter[Option[Float]]('f, Some("a form field")).toJson,
      formParamJson(false, "number", Some("float"))),

    ("Optional double form param",
      FormFieldParameter[Option[Double]]('f, Some("a form field")).toJson,
      formParamJson(false, "number", Some("double"))),

    ("Optional file form param",
      FormFieldParameter[Option[(FileInfo, Source[ByteString, Any])]]('f, Some("a form field")).toJson,
      formParamJson(false, "file")),

    // Optional query parameter types
    ("Optional string query",
      QueryParameter[Option[String]]('qp, Some("a description"), Some(Some("default-value"))).toJson,
      queryParamJson(false, "string", None, Some(JsString("default-value")))),

    ("Optional boolean query",
      QueryParameter[Option[Boolean]]('qp, Some("a description"), Some(Some(true))).toJson,
      queryParamJson(false, "boolean", None, Some(JsTrue))),

    ("Optional int query",
      QueryParameter[Option[Int]]('qp, Some("a description"), Some(Some(3))).toJson,
      queryParamJson(false, "integer", Some("int32"), Some(JsNumber(3)))),

    ("Optional long query",
      QueryParameter[Option[Long]]('qp, Some("a description"), Some(Some(3))).toJson,
      queryParamJson(false, "integer", Some("int64"), Some(JsNumber(3)))),

    ("Optional float query",
      QueryParameter[Option[Float]]('qp, Some("a description"), Some(Some(0.0f))).toJson,
      queryParamJson(false, "number", Some("float"), Some(JsNumber(0)))),

    ("Optional double query",
      QueryParameter[Option[Double]]('qp, Some("a description"), Some(Some(3.1415))).toJson,
      queryParamJson(false, "number", Some("double"), Some(JsNumber(3.1415)))),

    // Optional header parameter types
    ("Optional string header",
      HeaderParameter[Option[String]](Symbol("x-my-header"), Some("a header"), Some(Some("default-value"))).toJson,
      headerParamJson(false, "string", None, Some(JsString("default-value")))),

    ("Optional boolean header",
      HeaderParameter[Option[Boolean]](Symbol("x-my-header"), Some("a header"), Some(Some(true))).toJson,
      headerParamJson(false, "boolean", None, Some(JsTrue))),

    ("Optional int header",
      HeaderParameter[Option[Int]](Symbol("x-my-header"), Some("a header"), Some(Some(3))).toJson,
      headerParamJson(false, "integer", Some("int32"), Some(JsNumber(3)))),

    ("Optional long header",
      HeaderParameter[Option[Long]](Symbol("x-my-header"), Some("a header"), Some(Some(3))).toJson,
      headerParamJson(false, "integer", Some("int64"), Some(JsNumber(3)))),

    ("Optional float header",
      HeaderParameter[Option[Float]](Symbol("x-my-header"), Some("a header"), Some(Some(0.0f))).toJson,
      headerParamJson(false, "number", Some("float"), Some(JsNumber(0)))),

    ("Optional double header",
      HeaderParameter[Option[Double]](Symbol("x-my-header"), Some("a header"), Some(Some(3.1415))).toJson,
      headerParamJson(false, "number", Some("double"), Some(JsNumber(3.1415)))),

    // Body parameters
    ("Required body parameter",
      BodyParameter[Pet]('pet, Some("a description")).toJson,
      bodyParameterJson(true)),

    ("Optional body parameter",
      BodyParameter[Option[Pet]]('pet, Some("a description"), Some(Some(defaultPet))).toJson,
      bodyParameterJson(false, Some(defaultPet.toJson))
    ),

    // Enum usage
    ("Enum example query parameter",
      QueryParameter[Option[String]]('qp, description = Some("a description"), Some(Some(Statuses.placed.toString)), Some(Statuses.values.toList.map(value => Some(value.toString)))).toJson,
      queryParamJson(false, "string", None, Some(JsString("placed")), Some(JsArray(JsString("placed"), JsString("approved"), JsString("delivered"))))
    ),

    ("Enum example path parameter",
      PathParameter[String]('petId, Some("a description"), None, Some(Statuses.values.toList.map(value => value.toString))).toJson,
      pathParameterJson(true, "string", None, None, Some(JsArray(JsString("placed"), JsString("approved"), JsString("delivered"))))
    ),

    ("Enum example header parameter",
      HeaderParameter[String](Symbol("x-my-header"), Some("a header"), None, Some(Statuses.values.toList.map(value => value.toString))).toJson,
      headerParamJson(true, "string", None, None, Some(JsArray(JsString("placed"), JsString("approved"), JsString("delivered"))))
    ),

    ("HNil",
      (HNil: HNil).toJson,
      JsArray())
  )

  forAll(parameters) { (testcase, parameter, expectedJson) =>
    "ParametersJsonProtocol" should s"serialize $testcase according to the swagger schema" in {
      parameter shouldBe expectedJson
    }
  }

  it should "serialize an hlist of query params" in {

    val params =
      QueryParameter[Int]('r) :: PathParameter[String]('s) ::
        HeaderParameter[Int]('t) :: BodyParameter[String]('u) :: HNil

    val expectedJson = JsArray(
      JsObject(
        "name" -> JsString("r"),
        "in" -> JsString("query"),
        "required" -> JsBoolean(true),
        "type" -> JsString("integer"),
        "format" -> JsString("int32")
      ),
      JsObject(
        "name" -> JsString("s"),
        "in" -> JsString("path"),
        "required" -> JsBoolean(true),
        "type" -> JsString("string")
      ),
      JsObject(
        "name" -> JsString("t"),
        "in" -> JsString("header"),
        "required" -> JsBoolean(true),
        "type" -> JsString("integer"),
        "format" -> JsString("int32")
      ),
      JsObject(
        "name" -> JsString("u"),
        "in" -> JsString("body"),
        "required" -> JsBoolean(true),
        "schema" -> JsObject(
          "type" -> JsString("string")
        )
      ))

    params.toJson shouldBe expectedJson
  }

  private def queryParamJson(required: Boolean, `type`: String, format: Option[String] = None, default: Option[JsValue] = None, enum: Option[JsValue] = None) =
    jsObject(
      Some("name" -> JsString("qp")),
      Some("in" -> JsString("query")),
      Some("description" -> JsString("a description")),
      Some("required" -> JsBoolean(required)),
      Some("type" -> JsString(`type`)),
      format.map("format" -> JsString(_)),
      default.map("default" -> _),
      enum.map("enum" -> _)
    )

  private def headerParamJson(required: Boolean, `type`: String, format: Option[String] = None, default: Option[JsValue] = None, enum: Option[JsValue] = None) =
    jsObject(
      Some("name" -> JsString("x-my-header")),
      Some("in" -> JsString("header")),
      Some("description" -> JsString("a header")),
      Some("required" -> JsBoolean(required)),
      Some("type" -> JsString(`type`)),
      format.map("format" -> JsString(_)),
      default.map("default" -> _),
      enum.map("enum" -> _)
    )

  private def formParamJson(required: Boolean, `type`: String, format: Option[String] = None, default: Option[JsValue] = None, enum: Option[JsValue] = None) =
    jsObject(
      Some("name" -> JsString("f")),
      Some("in" -> JsString("formData")),
      Some("description" -> JsString("a form field")),
      Some("required" -> JsBoolean(required)),
      Some("type" -> JsString(`type`)),
      format.map("format" -> JsString(_)),
      default.map("default" -> _),
      enum.map("enum" -> _)
    )

  private def pathParameterJson(required: Boolean, `type`: String, format: Option[String] = None, default: Option[JsValue] = None, enum: Option[JsValue] = None) =
    jsObject(
      Some("name" -> JsString("petId")),
      Some("in" -> JsString("path")),
      Some("description" -> JsString("a description")),
      Some("required" -> JsBoolean(required)),
      Some("type" -> JsString(`type`)),
      format.map("format" -> JsString(_)),
      default.map("default" -> _),
      enum.map("enum" -> _)
    )

  private def bodyParameterJson(required: Boolean, default: Option[JsValue] = None) =
    jsObject(
      Some("name" -> JsString("pet")),
      Some("in" -> JsString("body")),
      Some("description" -> JsString("a description")),
      Some("required" -> JsBoolean(required)),
      default.map("default" -> _),
      Some(
        "schema" -> JsObject(
          "type" -> JsString("object"),
          "required" -> JsArray(JsString("petName")),
          "properties" -> JsObject(
            "petName" -> JsObject(
              "type" -> JsString("string")
            )
          )
        )
      )
    )
}
