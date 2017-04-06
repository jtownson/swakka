package net.jtownson.minimal

import spray.json.{JsArray, JsObject, JsValue}

object Flattener {

  def flattenToArray(jsValue: JsArray): JsArray = jsValue match {
    case JsArray(Vector(head, JsArray(tail: Seq[JsValue]))) =>
      JsArray(head :: tail.toList: _*)
    case _: JsValue =>
      jsValue
  }

  val toKeyVal: JsValue => Seq[(String, JsValue)] = {
    case JsObject(fields) => fields.toList
    case _ => Nil
  }

  def flattenToObject(jsValue: JsArray): JsObject = {
    val arrayOfFields: Seq[JsValue] = flattenToArray(jsValue).elements

    val extractedFields: Seq[(String, JsValue)] = arrayOfFields flatMap toKeyVal

    JsObject(extractedFields: _*)

  }
}
