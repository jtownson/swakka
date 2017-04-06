package net.jtownson.minimal

import spray.json.{JsValue, JsonFormat}

trait ParameterJsonFormat[T] extends JsonFormat[T] {
  def read(json: JsValue): T = throw new UnsupportedOperationException("Cannot read swagger files (yet).")
}

object ParameterJsonFormat {
  def func2Format[T](f: T => JsValue): ParameterJsonFormat[T] = (obj: T) => f(obj)
}
