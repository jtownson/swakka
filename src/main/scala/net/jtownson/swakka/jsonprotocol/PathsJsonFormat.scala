package net.jtownson.swakka.jsonprotocol

import spray.json.{JsValue, JsonFormat}

trait PathsJsonFormat[T] extends JsonFormat[T] {
  def read(json: JsValue): T = throw new UnsupportedOperationException("Cannot read swagger files (yet).")
}

object PathsJsonFormat {
  def func2Format[T](f: T => JsValue): PathsJsonFormat[T] = (obj: T) => f(obj)
}
