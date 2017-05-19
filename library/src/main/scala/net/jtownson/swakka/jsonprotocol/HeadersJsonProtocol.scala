package net.jtownson.swakka.jsonprotocol

import net.jtownson.swakka.model.Responses.Header
import net.jtownson.swakka.jsonprotocol.Flattener.flattenToObject
import shapeless.{::, HList, HNil}
import spray.json.{JsArray, JsNull, JsObject, JsString, JsValue}
import HeadersJsonFormat._
import net.jtownson.swakka.misc.jsObject

trait HeadersJsonProtocol {

  implicit val stringHeaderFormat: HeadersJsonFormat[Header[String]] =
    func2Format(header => headerJson(header.name, "string", None, header.description))

  implicit val doubleHeaderFormat: HeadersJsonFormat[Header[Double]] =
    func2Format(header => headerJson(header.name, "number", Some("double"), header.description))

  implicit val floatHeaderFormat: HeadersJsonFormat[Header[Float]] =
    func2Format(header => headerJson(header.name, "number", Some("float"), header.description))

  implicit val integerHeaderFormat: HeadersJsonFormat[Header[Int]] =
    func2Format(header => headerJson(header.name, "integer", Some("int32"), header.description))

  implicit val longHeaderFormat: HeadersJsonFormat[Header[Long]] =
    func2Format(header => headerJson(header.name, "integer", Some("int64"), header.description))

  implicit val booleanHeaderFormat: HeadersJsonFormat[Header[Boolean]] =
    func2Format(header => headerJson(header.name, "boolean", None, header.description))

  // TODO
  //  implicit val arrayHeaderFormat: HeadersJsonFormat[Header[String]] = ???

  implicit val hNilHeaderFormat: HeadersJsonFormat[HNil] =
    _ => JsNull

  private def headerJson(name: Symbol, `type`: String, format: Option[String], description: Option[String]): JsValue = {
    JsObject(
      name.name -> jsObject(
        Some("type" -> JsString(`type`)),
        format.map("format" -> JsString(_)),
        description.map("description" -> JsString(_))
      ))
  }

  implicit def hConsHeaderFormat[H, T <: HList](implicit head: HeadersJsonFormat[H], tail: HeadersJsonFormat[T]): HeadersJsonFormat[H :: T] =
    func2Format((l: H :: T) => {
      flattenToObject(JsArray(head.write(l.head), tail.write(l.tail)))
    })
}

object HeadersJsonProtocol extends HeadersJsonProtocol