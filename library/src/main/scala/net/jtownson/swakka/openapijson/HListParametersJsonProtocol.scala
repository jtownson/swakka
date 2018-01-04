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

import net.jtownson.swakka.jsonschema.{JsonSchema, SchemaWriter}
import net.jtownson.swakka.misc.jsObject
import net.jtownson.swakka.openapimodel.{Parameter, _}
import ParameterJsonFormat.instance
import shapeless.{::, Generic, HList, HNil, |¬|}
import spray.json._


trait HListParametersJsonProtocol {

  implicit val hNilParamFormat: ParameterJsonFormat[HNil] =
    _ => JsArray()

  implicit def hConsParamFormat[H, T <: HList](
      implicit head: ParameterJsonFormat[H],
      tail: ParameterJsonFormat[T]): ParameterJsonFormat[H :: T] =
    instance((l: H :: T) => {
      Flattener.flattenToArray(JsArray(head.write(l.head), tail.write(l.tail)))
    })

  // Because the Paramter types are Products, with a Generic.Aux, the compiler occasionally
  // (and apparently non-deterministically) goes down the wrong route and diverges.
  // Use shapeless's |¬| to force use of the more specific JsonFormats for Parameter types.
  implicit def genericParamFormat[Params: |¬|[Parameter[_]]#λ, ParamsList]
  (implicit gen: Generic.Aux[Params, ParamsList],
   ev: ParameterJsonFormat[ParamsList]): ParameterJsonFormat[Params] =
    ParameterJsonFormat.instance(params => ev.write(gen.to(params)))


}

object HListParametersJsonProtocol extends HListParametersJsonProtocol
