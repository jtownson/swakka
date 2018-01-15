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

import akka.http.scaladsl.server.directives.FileInfo
import akka.stream.scaladsl.Source
import akka.util.ByteString
import spray.json.{JsArray, JsBoolean, JsObject, JsString, JsValue, _}

import net.jtownson.swakka.misc.jsObject
import net.jtownson.swakka.openapimodel._

import org.scalatest._
import org.scalatest.Matchers._
import org.scalatest.prop.TableDrivenPropertyChecks._

import shapeless.HNil

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

    // Constrained PathParameters
    ("Required, constrained string QueryParam",
      QueryParameterConstrained[String, String](
        name = 'petId,
        description = Some("a description"),
        constraints = Constraints[String](minLength = Some(1), maxLength = Some(10), pattern = Some("\\w+"), enum = Some(Set("foo", "bar")))).toJson,
      constrainedParamJson(name = "petId", in = "query", `type` = "string", minLength = Some(JsNumber(1)), maxLength = Some(JsNumber(10)), pattern = Some(JsString("\\w+")), enum = Some(JsArray(JsString("foo"), JsString("bar"))))
    ),

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

    // Constrained PathParameters
    ("Required, constrained string PathParam",
      PathParameterConstrained[String, String](
        name = 'petId,
        description = Some("a description"),
        constraints = Constraints[String](minLength = Some(1), maxLength = Some(10), pattern = Some("\\w+"), enum = Some(Set("foo", "bar")))).toJson,
      constrainedParamJson(name = "petId", in = "path", `type` = "string", minLength = Some(JsNumber(1)), maxLength = Some(JsNumber(10)), pattern = Some(JsString("\\w+")), enum = Some(JsArray(JsString("foo"), JsString("bar"))))
    ),

    // (Required) path parameter types
    ("Required string path",
      PathParameter[String]('petId, Some("a description")).toJson,
      pathParameterJson("string")),

    ("Required boolean path",
      PathParameter[Boolean]('petId, Some("a description")).toJson,
      pathParameterJson("boolean")),

    ("Required int path",
      PathParameter[Int]('petId, Some("a description")).toJson,
      pathParameterJson("integer", Some("int32"))),

    ("Required long path",
      PathParameter[Long]('petId, Some("a description")).toJson,
      pathParameterJson("integer", Some("int64"))),

    ("Required float path",
      PathParameter[Float]('petId, Some("a description")).toJson,
      pathParameterJson("number", Some("float"))),

    ("Required double path",
      PathParameter[Double]('petId, Some("a description")).toJson,
      pathParameterJson("number", Some("double"))),

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

    // Required, constrained header parameter types
    ("Required, constrained string HeaderParam",
      HeaderParameterConstrained[String, String](
        name = 'petId,
        description = Some("a description"),
        constraints = Constraints[String](minLength = Some(1), maxLength = Some(10), pattern = Some("\\w+"))).toJson,
      constrainedParamJson(name = "petId", in = "header", `type` = "string", minLength = Some(JsNumber(1)), maxLength = Some(JsNumber(10)), pattern = Some(JsString("\\w+")))
    ),

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
      pathParameterJson("string", None, None, Some(JsArray(JsString("placed"), JsString("approved"), JsString("delivered"))))
    ),

    ("Enum example header parameter",
      HeaderParameter[String](Symbol("x-my-header"), Some("a header"), None, Some(Statuses.values.toList.map(value => value.toString))).toJson,
      headerParamJson(true, "string", None, None, Some(JsArray(JsString("placed"), JsString("approved"), JsString("delivered"))))
    ),

    ("HNil",
      HListParametersJsonProtocol.hNilParamFormat.write(HNil: HNil),
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

  private def pathParameterJson(`type`: String, format: Option[String] = None, default: Option[JsValue] = None, enum: Option[JsValue] = None) =
    jsObject(
      Some("name" -> JsString("petId")),
      Some("in" -> JsString("path")),
      Some("description" -> JsString("a description")),
      Some("required" -> JsTrue),
      Some("type" -> JsString(`type`)),
      format.map("format" -> JsString(_)),
      default.map("default" -> _),
      enum.map("enum" -> _)
    )

  private def constrainedParamJson(name: String, in: String, `type`: String, format: Option[String] = None, default: Option[JsValue] = None,
                                        multipleOf: Option[JsValue] = None,
                                        maximum: Option[JsValue] = None, minimum: Option[JsValue] = None,
                                        exlusiveMaximum: Option[JsValue] = None, exclusiveMinimum: Option[JsValue] = None,
                                        maxLength: Option[JsValue] = None, minLength: Option[JsValue] = None, pattern: Option[JsValue] = None,
                                        items: Option[JsValue] = None, maxItems: Option[JsValue] = None, minItems: Option[JsValue] = None,
                                        uniqueItems: Option[JsValue] = None, contains: Option[JsValue] = None,
                                        enum: Option[JsValue] = None) =
    jsObject(
      Some("name" -> JsString(name)),
      Some("in" -> JsString(in)),
      Some("description" -> JsString("a description")),
      Some("required" -> JsTrue),
      Some("type" -> JsString(`type`)),
      format.map("format" -> JsString(_)),
      default.map("default" -> _),
      enum.map("enum" -> _),
      multipleOf.map("multipleOf" -> _),
      maximum.map("maximum" -> _),
      minimum.map("minimum" -> _),
      exlusiveMaximum.map("exlusiveMaximum" -> _),
      exclusiveMinimum.map("exclusiveMinimum" -> _),
      maxLength.map("maxLength" -> _),
      minLength.map("minLength" -> _),
      pattern.map("pattern" -> _),
      items.map("items" -> _),
      maxItems.map("maxItems" -> _),
      minItems.map("minItems" -> _),
      uniqueItems.map("uniqueItems" -> _),
      contains.map("contains" -> _)
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
