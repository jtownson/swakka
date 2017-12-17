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

import net.jtownson.swakka.jsonschema.{JsonSchema, SchemaWriter}
import net.jtownson.swakka.misc.jsObject
import net.jtownson.swakka.openapijson.Flattener.flattenToObject
import net.jtownson.swakka.openapijson.ResponseJsonFormat._
import net.jtownson.swakka.openapijson.{HeadersJsonFormat, ResponseJsonFormat}
import net.jtownson.swakka.openapimodel._
import shapeless.labelled.FieldType
import shapeless.{::, Generic, HList, HNil, LabelledGeneric, Lazy, Witness, |¬|}
import spray.json.{JsArray, JsNull, JsObject, JsString, JsValue}

trait ResponsesJsonProtocol2 {

  implicit val hNilResponseFormat: ResponseJsonFormat[HNil] =
    _ => JsObject()


//  implicit def hConsResponseFormat[H, T <: HList]
//  (implicit head: ResponseJsonFormat[H], tail: ResponseJsonFormat[T]): ResponseJsonFormat[H :: T] =
//    instance((l: H :: T) => {
//      flattenToObject(JsArray(head.write(l.head), tail.write(l.tail)))
//    })

  // 1 this needs LabelledGeneric.Aux[Responses, ResponsesList]
  // 2 add Lazy
//  implicit def genericResponseFormat[Responses: |¬|[ResponseValue[_, _]]#λ, ResponsesList]
//  (implicit gen: Generic.Aux[Responses, ResponsesList],
//   ev: Lazy[ResponseJsonFormat[ResponsesList]]): ResponseJsonFormat[Responses] =
//    instance(responses => ev.value.write(gen.to(responses)))


//  implicit def genericResponseFormat[Responses: |¬|[ResponseValue[_, _]]#λ, ResponsesList]
//  (implicit gen: LabelledGeneric.Aux[Responses, ResponsesList],
//   ev: Lazy[ResponseJsonFormat[ResponsesList]]): ResponseJsonFormat[Responses] =
//    instance(responses => ev.value.write(gen.to(responses)))
//
//  // 3 write a record
//
    implicit def recordWriter[K <: Symbol, H, T <: HList](implicit
                                                        witness: Witness.Aux[K],
                                                        hWriter: Lazy[ResponseJsonFormat[H]],
                                                        tWriter: ResponseJsonFormat[T])
  : ResponseJsonFormat[FieldType[K, H] :: T] =
    instance((hl: FieldType[K, H] :: T) => {
      JsObject(
        JsObject(witness.value.name -> hWriter.value.write(hl.head)).fields ++
          tWriter.write(hl.tail).asInstanceOf[JsObject].fields
      )
    }
    )


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

