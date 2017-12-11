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

import spray.json.{JsArray, JsObject, JsValue}

object Flattener {

  def flattenToArray(jsValue: JsArray): JsArray = jsValue match {
    case JsArray(Vector(head, JsArray(tail: Seq[JsValue]))) =>
      head match {
        case JsArray(headFields: Seq[JsValue]) =>
          JsArray(headFields.toList ++ tail.toList: _*)
        case _ => JsArray(head :: tail.toList: _*)
      }
    case _: JsValue =>
      jsValue
  }

  val toKeyVal: JsValue => Seq[(String, JsValue)] = {
    case JsObject(fields) => fields.toList
    case _ => Nil
  }

  private val mergeValues: (JsValue, JsValue) => JsValue = (acc, nextJsValue) => {
    (acc, nextJsValue) match {
      case (JsObject(accFields), JsObject(nextFields)) => JsObject(accFields ++ nextFields)
      case _ => acc
    }
  }


  def merge(extractedFields: Seq[(String, JsValue)]): Map[String, JsValue] = {

    val groupBy: Map[String, Seq[(String, JsValue)]] = extractedFields.groupBy(_._1)

    groupBy.map(t => {
      val (key, vals: Seq[(String, JsValue)]) = t
      if (vals.size == 1)
        vals.head
      else
        (key, vals.map(_._2).reduceLeft(mergeValues))
    })
  }

  def flattenToObject(jsValue: JsArray): JsObject = {
    val arrayOfFields: Seq[JsValue] = flattenToArray(jsValue).elements

    val extractedFields: Seq[(String, JsValue)] = arrayOfFields flatMap toKeyVal

    val mergedFields: Map[String, JsValue] = merge(extractedFields)

    JsObject(mergedFields)
  }
}
