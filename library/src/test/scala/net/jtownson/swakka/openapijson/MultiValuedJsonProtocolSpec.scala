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

import net.jtownson.swakka.openapimodel._
import net.jtownson.swakka.misc.jsObject
import org.scalatest.FlatSpec
import org.scalatest.Matchers._
import org.scalatest.prop.TableDrivenPropertyChecks._
import spray.json.{JsArray, JsBoolean, JsString}
import spray.json._

class MultiValuedJsonProtocolSpec extends FlatSpec {

  "MultiValuedJsonProtocol" should "serialize collection formats" in {

    val qp = QueryParameterConstrained[String, String](
      name = 'status,
      description = Some("Status values that need to be considered for filter"),
      default = Some("available"),
      constraints =
        Constraints(enum = Some(Set("available", "pending", "sold")))
    )

    val mqp =
      MultiValued[String, QueryParameterConstrained[String, String]](qp, multi)

    mqp.toJson shouldBe arrayJson(
      JsString("query"),
      JsString("string"),
      JsString("multi"),
      JsString("Status values that need to be considered for filter"),
      Some(
        JsArray(JsString("available"), JsString("pending"), JsString("sold")))
    )
  }

  private def arrayJson(in: JsString,
                        itemType: JsString,
                        collectionFormat: JsString,
                        description: JsString,
                        enum: Option[JsArray]): JsValue =
    jsObject(
      Some("name" -> JsString("status")),
      Some("in" -> in),
      Some("description" -> description),
      Some("required" -> JsBoolean(true)),
      Some("type" -> JsString("array")),
      Some(
        "items" -> jsObject(
          Some("type" -> itemType),
          enum.map("enum" -> _)
        )),
      Some("collectionFormat" -> collectionFormat)
    )

  it should "support multi format for form fields" in {
    val fdp = FormFieldParameter[String]('status, Some("a description"))

    val mfp = MultiValued[String, FormFieldParameter[String]](fdp, multi)

    mfp.toJson shouldBe arrayJson(JsString("formData"),
                                  JsString("string"),
                                  JsString("multi"),
                                  JsString("a description"),
                                  None)
  }

  it should "support multi format for path params" in {
    val pp = PathParameter[String]('status, Some("a description"))

    val mpp = MultiValued[String, PathParameter[String]](pp, multi)

    mpp.toJson shouldBe arrayJson(JsString("path"),
                                  JsString("string"),
                                  JsString("multi"),
                                  JsString("a description"),
                                  None)

  }

  it should "support multi format for header params" in {
    val hp = HeaderParameter[String]('status, Some("a description"))

    val mhp = MultiValued[String, HeaderParameter[String]](hp, multi)

    mhp.toJson shouldBe arrayJson(JsString("header"),
                                  JsString("string"),
                                  JsString("multi"),
                                  JsString("a description"),
                                  None)

  }

  val formatCases = Table(("description", "format"),
                          ("Pipe separated", pipes),
                          ("Comma separated", csv),
                          ("Tab separated", tsv),
                          ("Space separated", ssv))

  forAll(formatCases) { (description, collectionFormat) =>
    s"$description" should "work for json serialization" in {

      MultiValued[String, HeaderParameter[String]](
        HeaderParameter[String]('status, Some("a description")),
        collectionFormat).toJson shouldBe arrayJson(
        JsString("header"),
        JsString("string"),
        JsString(collectionFormat.toString),
        JsString("a description"),
        None)

      MultiValued[String, QueryParameter[String]](
        QueryParameter[String]('status, Some("a description")),
        collectionFormat).toJson shouldBe arrayJson(
        JsString("query"),
        JsString("string"),
        JsString(collectionFormat.toString),
        JsString("a description"),
        None)

      MultiValued[String, FormFieldParameter[String]](
        FormFieldParameter[String]('status, Some("a description")),
        collectionFormat).toJson shouldBe arrayJson(
        JsString("formData"),
        JsString("string"),
        JsString(collectionFormat.toString),
        JsString("a description"),
        None)

      MultiValued[String, PathParameter[String]](
        PathParameter[String]('status, Some("a description")),
        collectionFormat).toJson shouldBe arrayJson(
        JsString("path"),
        JsString("string"),
        JsString(collectionFormat.toString),
        JsString("a description"),
        None)
    }
  }
}
