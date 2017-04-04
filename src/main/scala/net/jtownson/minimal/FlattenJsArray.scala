package net.jtownson.minimal

import spray.json.{JsArray, JsValue}

object FlattenJsArray {

  def flatten(jsValue: JsValue): JsValue = jsValue match {
    case JsArray(Vector(head, JsArray(tail: Seq[JsValue]))) =>
      JsArray(head :: tail.toList: _*)
    case _: JsValue =>
      jsValue
  }
}
