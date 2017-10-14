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

import net.jtownson.swakka.model.Parameters.{MultiValued, QueryParameter}
import org.scalatest.FlatSpec
import org.scalatest.Matchers._
import spray.json.{JsArray, JsBoolean, JsObject, JsString}
import ParametersJsonProtocol._
import spray.json._

class MultiValuedJsonProtocolSpec extends FlatSpec {

  "MultiValuedJsonProtocol" should "serialize collection formats" in {

    val qp = QueryParameter[String](
      name = 'status,
      description = Some("Status values that need to be considered for filter"),
      default = Some("available"),
      enum = Some(Seq("available", "pending", "sold"))
    )

    val mqp = MultiValued[String, QueryParameter[String]](qp)

    val expectedJson =
      JsObject(
        "name" -> JsString("status"),
        "in" -> JsString("query"),
        "description" -> JsString("Status values that need to be considered for filter"),
        "required" -> JsBoolean(true),
        "type" -> JsString("array"),
        "items" -> JsObject(
          "type" -> JsString("string"),
          "enum" -> JsArray(
            JsString("available"),
            JsString("pending"),
            JsString("sold")
          )
        ),
        "collectionFormat" -> JsString("multi")
      )

    mqp.toJson shouldBe expectedJson
  }


}
