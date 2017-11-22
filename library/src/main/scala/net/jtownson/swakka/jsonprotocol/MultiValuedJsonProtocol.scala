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

import net.jtownson.swakka.misc.jsObject
import net.jtownson.swakka.OpenApiModel._
import spray.json._

trait MultiValuedJsonProtocol {

  val pullUpFields = Set("description", "in", "name", "required")

  implicit def multiReqParamFormat[T, U <: Parameter[T]](implicit evu: ParameterJsonFormat[U]):
  ParameterJsonFormat[MultiValued[T, U]] =
    new ParameterJsonFormat[MultiValued[T, U]] {
      override def write(mqp: MultiValued[T, U]): JsValue = {
        val itemValue: JsObject = evu.write(mqp.singleParam).asJsObject
        val filteredValue: Map[String, JsValue] = itemValue.fields.filterKeys(!pullUpFields.contains(_))
        arrayParam(mqp.name, itemValue.fields("in"), mqp.description, true, None, JsObject(filteredValue))
      }
    }


  private def arrayParam[Item](name: Symbol, in: JsValue, description: Option[String],
                               required: Boolean,
                               default: Option[JsValue] = None,
                               itemValue: JsValue): JsValue = {

    jsObject(
      Some("name" -> JsString(name.name)),
      Some("in" -> in),
      description.map("description" -> JsString(_)),
      Some("required" -> JsBoolean(required)),
      Some("type" -> JsString("array")),
      Some("items" -> itemValue),
      default.map("default" -> _),
      Some("collectionFormat" -> JsString("multi"))
    )
  }

}

object MultiValuedJsonProtocol extends MultiValuedJsonProtocol