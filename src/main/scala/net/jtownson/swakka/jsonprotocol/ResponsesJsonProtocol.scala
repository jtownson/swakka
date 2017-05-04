package net.jtownson.swakka.jsonprotocol

import net.jtownson.swakka.OpenApiModel.ResponseValue
import net.jtownson.swakka.jsonprotocol.Flattener.flattenToObject
import net.jtownson.swakka.jsonprotocol.ResponseJsonFormat._
import net.jtownson.swakka.jsonschema.{JsonSchema, SchemaWriter}
import net.jtownson.swakka.misc.jsObject
import shapeless.{::, HList, HNil}
import spray.json.{JsArray, JsNull, JsObject, JsString, JsValue}

trait ResponsesJsonProtocol {

  implicit val hNilResponseFormat: ResponseJsonFormat[HNil] =
    _ => JsObject()


  implicit def hConsResponseFormat[H, T <: HList](implicit head: ResponseJsonFormat[H], tail: ResponseJsonFormat[T]): ResponseJsonFormat[H :: T] =
    func2Format((l: H :: T) => {
      flattenToObject(JsArray(head.write(l.head), tail.write(l.tail)))
    })


  implicit def responseFormat[T: SchemaWriter, Headers: HeadersJsonFormat]: ResponseJsonFormat[ResponseValue[T, Headers]] =
    func2Format(rv => swaggerResponse(rv.responseCode, rv.description, JsonSchema[T](), rv.headers))


  private def swaggerResponse[T, Headers](status: String, description: String, schema: JsonSchema[T], headers: Headers)
                                         (implicit sw: SchemaWriter[T], hf: HeadersJsonFormat[Headers]): JsValue =
    JsObject(
      status -> jsObject(
        Some("description" -> JsString(description)),
        filteringJsNull(hf.write(headers)).map("headers" -> _),
        filteringJsNull(sw.write(schema)).map("schema" -> _)
      )
    )

  private def filteringJsNull(jsValue: JsValue): Option[JsValue] = jsValue match {
    case JsNull => None
    case _ => Some(jsValue)
  }
}

object ResponsesJsonProtocol extends ResponsesJsonProtocol