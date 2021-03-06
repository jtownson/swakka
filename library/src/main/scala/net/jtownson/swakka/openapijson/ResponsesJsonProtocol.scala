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

import net.jtownson.swakka.openapijson.Flattener.flattenToObject
import net.jtownson.swakka.openapijson.ResponseJsonFormat._
import net.jtownson.swakka.jsonschema.{JsonSchema, SchemaWriter}
import net.jtownson.swakka.misc.jsObject
import net.jtownson.swakka.openapimodel._
import shapeless.{::, Generic, HList, HNil, |¬|}
import spray.json.{JsArray, JsNull, JsObject, JsString, JsValue}

trait ResponsesJsonProtocol {

  implicit val hNilResponseFormat: ResponseJsonFormat[HNil] =
    instance(_ => JsObject())


  implicit def hConsResponseFormat[H, T <: HList]
  (implicit head: ResponseJsonFormat[H], tail: ResponseJsonFormat[T]): ResponseJsonFormat[H :: T] =
    instance((l: H :: T) => {
      flattenToObject(JsArray(head.write(l.head), tail.write(l.tail)))
    })

  implicit def genericResponseFormat[Responses: |¬|[ResponseValue[_, _]]#λ, ResponsesList]
  (implicit gen: Generic.Aux[Responses, ResponsesList],
   ev: ResponseJsonFormat[ResponsesList]): ResponseJsonFormat[Responses] =
    instance(responses => ev.write(gen.to(responses)))

  implicit def responseFormat[T: SchemaWriter, Headers: HeadersJsonFormat]:
  ResponseJsonFormat[ResponseValue[T, Headers]] =
    instance(rv => swaggerResponse(rv.responseCode, rv.description, JsonSchema[T](), rv.headers))


  private def swaggerResponse[T, Headers](status: String, description: String, schema: JsonSchema[T], headers: Headers)
                                         (implicit sw: SchemaWriter[T], hf: HeadersJsonFormat[Headers]): JsValue =
    JsObject(
      status -> jsObject(
        Some("description" -> JsString(description)),
        filteringJsNull(hf.write(headers)).map("headers" -> _),
        filteringJsNull(sw.write(schema)).map("schema" -> _)
      )
    )

  private def filteringJsNull(jsValue: JsValue): Option[JsValue] = jsValue match {
    case JsNull => None
    case _ => Some(jsValue)
  }
}

object ResponsesJsonProtocol extends ResponsesJsonProtocol