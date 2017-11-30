package net.jtownson.swakka.jsonprotocol

import spray.json.{DeserializationException, JsString, JsValue, RootJsonFormat}

/**
  * Borrowed from https://github.com/spray/spray-json/issues/200
  */
class EnumJsonConverter[T <: scala.Enumeration](enu: T) extends RootJsonFormat[T#Value] {
  override def write(obj: T#Value): JsValue = JsString(obj.toString)

  override def read(json: JsValue): T#Value = {
    json match {
      case JsString(txt) => enu.withName(txt)
      case somethingElse => throw DeserializationException(s"Expected a value from enum $enu instead of $somethingElse")
    }
  }
}
