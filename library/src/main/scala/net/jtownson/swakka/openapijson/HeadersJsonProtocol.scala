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
import net.jtownson.swakka.openapijson.Flattener.flattenToObject
import net.jtownson.swakka.misc.jsObject
import HeadersJsonFormat._

import shapeless.{::, HList, HNil}
import spray.json.{JsArray, JsNull, JsObject, JsString, JsValue}

trait HeadersJsonProtocol {

  implicit val stringHeaderFormat: HeadersJsonFormat[Header[String]] =
    instance(header => headerJson(header.name, "string", None, header.description))

  implicit val doubleHeaderFormat: HeadersJsonFormat[Header[Double]] =
    instance(header => headerJson(header.name, "number", Some("double"), header.description))

  implicit val floatHeaderFormat: HeadersJsonFormat[Header[Float]] =
    instance(header => headerJson(header.name, "number", Some("float"), header.description))

  implicit val integerHeaderFormat: HeadersJsonFormat[Header[Int]] =
    instance(header => headerJson(header.name, "integer", Some("int32"), header.description))

  implicit val longHeaderFormat: HeadersJsonFormat[Header[Long]] =
    instance(header => headerJson(header.name, "integer", Some("int64"), header.description))

  implicit val booleanHeaderFormat: HeadersJsonFormat[Header[Boolean]] =
    instance(header => headerJson(header.name, "boolean", None, header.description))

  // TODO
  //  implicit val arrayHeaderFormat: HeadersJsonFormat[Header[String]] = ???

  implicit val hNilHeaderFormat: HeadersJsonFormat[HNil] =
    _ => JsNull

  private def headerJson(name: Symbol, `type`: String, format: Option[String], description: Option[String]): JsValue = {
    JsObject(
      name.name -> jsObject(
        Some("type" -> JsString(`type`)),
        format.map("format" -> JsString(_)),
        description.map("description" -> JsString(_))
      ))
  }

  implicit def hConsHeaderFormat[H, T <: HList](implicit head: HeadersJsonFormat[H], tail: HeadersJsonFormat[T]): HeadersJsonFormat[H :: T] =
    instance((l: H :: T) => {
      flattenToObject(JsArray(head.write(l.head), tail.write(l.tail)))
    })
}

object HeadersJsonProtocol extends HeadersJsonProtocol