package net.jtownson.swakka.misc

import spray.json.{JsObject, JsValue}

object jsObject {
  def apply(fields: Option[(String, JsValue)]*): JsObject = JsObject(fields.flatten: _*)
}
