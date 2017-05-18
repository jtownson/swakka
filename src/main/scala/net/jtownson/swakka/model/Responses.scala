package net.jtownson.swakka.model

import shapeless.HNil

object Responses {

  case class Header[T](name: Symbol, description: Option[String] = None)

  case class ResponseValue[T, Headers](responseCode: String, description: String, headers: Headers = HNil)
}
