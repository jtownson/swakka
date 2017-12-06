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

package net.jtownson.swakka.jsonschema

import net.jtownson.swakka.misc.jsObject
import spray.json.{JsArray, JsObject, JsString, JsValue}

object Schemas {

  val unitSchema = JsObject()

  def stringSchema(description: Option[String]) =
    jsObject(
      Some("type" -> JsString("string")),
      description.map("description" -> JsString(_))
    )

  def numericSchema(description: Option[String], `type`: String, format: Option[String]) =
    jsObject(
      Some("type" -> JsString(`type`)),
      description.map("description" -> JsString(_)),
      format.map("format" -> JsString(_))
    )

  def booleanSchema(description: Option[String]) =
    jsObject(
      Some("type" -> JsString("boolean")),
      description.map("description" -> JsString(_))
    )

  def arraySchema(description: Option[String], itemSchema: JsValue) =
    jsObject(
      Some("type", JsString("array")),
      description.map("description" -> JsString(_)),
      Some("items" -> itemSchema)
    )

  def mapSchema(description: Option[String], keySchema: JsValue) =
    jsObject(
      Some("type", JsString("object")),
      description.map("description" -> JsString(_)),
      Some("additionalProperties" -> keySchema)
    )

  def objectSchema(description: Option[String], requiredFields: List[String], fieldSchemas: List[(String, JsValue)]) = {
    jsObject(
      Some("type" -> JsString("object")),
      description.map("description" -> JsString(_)),
      optionalJsArray(requiredFields).map("required" -> _),
      Some("properties" -> JsObject(fieldSchemas: _*))
    )
  }

  def dateSchema(description: Option[String]) =
    jsObject(
      Some("type" -> JsString("string")),
      description.map("description" -> JsString(_)),
      Some("format" -> JsString("date-time"))
    )

  private def optionalJsArray(requiredFields: List[String]): Option[JsArray] =
    optionally(requiredFields).map(requiredFields => requiredFields.map(JsString(_))).map(JsArray(_: _*))

  private def optionally[T](l: List[T]): Option[List[T]] = l match {
    case Nil => None
    case _ => Some(l)
  }
}
