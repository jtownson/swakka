package net.jtownson.swakka.jsonprotocol

import spray.json.{JsValue, JsonFormat}

trait SecurityDefinitionsJsonFormat[T] extends JsonFormat[T] {
  def read(json: JsValue): T = throw new UnsupportedOperationException("Cannot read swagger security definitions yet.")
}

object SecurityDefinitionsJsonFormat {
  def func2Format[T](f: T => JsValue): SecurityDefinitionsJsonFormat[T] = (obj: T) => f(obj)
}
