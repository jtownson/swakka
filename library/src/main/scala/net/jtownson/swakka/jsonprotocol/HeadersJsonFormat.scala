package net.jtownson.swakka.jsonprotocol

import spray.json.{JsValue, JsonFormat}

trait HeadersJsonFormat[T] extends JsonFormat[T] {
  def read(json: JsValue): T = throw new UnsupportedOperationException("Cannot read swagger files (yet).")
}

object HeadersJsonFormat {
  def func2Format[T](f: T => JsValue): HeadersJsonFormat[T] = (obj: T) => f(obj)
}
