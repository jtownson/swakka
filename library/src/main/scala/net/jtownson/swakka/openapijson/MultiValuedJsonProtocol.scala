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

import net.jtownson.swakka.misc.jsObject
import net.jtownson.swakka.openapimodel._
import spray.json._

trait MultiValuedJsonProtocol {

  private val pullUpFields = Set("description", "in", "name", "required")

  implicit def multiQueryParamFormat[T](
      implicit evu: ParameterJsonFormat[QueryParameter[T]])
    : ParameterJsonFormat[MultiValued[T, QueryParameter[T]]] =
    multiFormat(evu)

  implicit def multiQueryParamConstrainedFormat[T, U](
      implicit evu: ParameterJsonFormat[QueryParameterConstrained[T, U]])
    : ParameterJsonFormat[MultiValued[T, QueryParameterConstrained[T, U]]] =
    multiFormat(evu)

  implicit def multiFormFieldParamFormat[T](
      implicit evu: ParameterJsonFormat[FormFieldParameter[T]])
    : ParameterJsonFormat[MultiValued[T, FormFieldParameter[T]]] =
    multiFormat(evu)

  implicit def multiFormFieldConstrainedParamFormat[T, U](
      implicit evu: ParameterJsonFormat[FormFieldParameterConstrained[T, U]])
    : ParameterJsonFormat[MultiValued[T, FormFieldParameterConstrained[T, U]]] =
    multiFormat(evu)

  implicit def multiPathParamFormat[T](
      implicit evu: ParameterJsonFormat[PathParameter[T]])
    : ParameterJsonFormat[MultiValued[T, PathParameter[T]]] =
    multiFormat(evu)

  implicit def multiPathParamConstrainedParamFormat[T, U](
      implicit evu: ParameterJsonFormat[PathParameterConstrained[T, U]])
    : ParameterJsonFormat[MultiValued[T, PathParameterConstrained[T, U]]] =
    multiFormat(evu)

  implicit def multiHeaderParamFormat[T](
      implicit evu: ParameterJsonFormat[HeaderParameter[T]])
    : ParameterJsonFormat[MultiValued[T, HeaderParameter[T]]] =
    multiFormat(evu)

  implicit def multiHeaderParamConstrainedParamFormat[T, U](
      implicit evu: ParameterJsonFormat[HeaderParameterConstrained[T, U]])
    : ParameterJsonFormat[MultiValued[T, HeaderParameterConstrained[T, U]]] =
    multiFormat(evu)

  private def multiFormat[T, P <: Parameter[T]](
      pjf: ParameterJsonFormat[P]): ParameterJsonFormat[MultiValued[T, P]] =
    new ParameterJsonFormat[MultiValued[T, P]] {
      override def write(mqp: MultiValued[T, P]): JsValue = {
        val itemValue: JsObject = pjf.write(mqp.singleParam).asJsObject
        val filteredValue: Map[String, JsValue] =
          itemValue.fields.filterKeys(!pullUpFields.contains(_))
        arrayParam(mqp.name,
                   itemValue.fields("in"),
                   mqp.description,
                   true,
                   None,
                   JsObject(filteredValue),
                   JsString(mqp.collectionFormat.toString))
      }
    }

  private def arrayParam[Item](name: Symbol,
                               in: JsValue,
                               description: Option[String],
                               required: Boolean,
                               default: Option[JsValue] = None,
                               itemValue: JsValue,
                               collectionFormat: JsValue): JsValue =
    jsObject(
      Some("name" -> JsString(name.name)),
      Some("in" -> in),
      description.map("description" -> JsString(_)),
      Some("required" -> JsBoolean(required)),
      Some("type" -> JsString("array")),
      Some("items" -> itemValue),
      default.map("default" -> _),
      Some("collectionFormat" -> collectionFormat)
    )

}

object MultiValuedJsonProtocol extends MultiValuedJsonProtocol
