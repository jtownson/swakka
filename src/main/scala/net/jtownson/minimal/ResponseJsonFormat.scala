package net.jtownson.minimal

import spray.json.{JsValue, JsonFormat}

trait ResponseJsonFormat[T] extends JsonFormat[T] {
  def read(json: JsValue): T = throw new UnsupportedOperationException("Cannot read swagger files (yet).")
}

object ResponseJsonFormat {
  def func2Format[T](f: T => JsValue): ResponseJsonFormat[T] = (obj: T) => f(obj)
}
