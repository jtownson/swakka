package net.jtownson.swakka.jsonprotocol

import Flattener.flattenToObject
import net.jtownson.swakka.OpenApiModel.ResponseValue
import net.jtownson.swakka.jsonprotocol.ResponseJsonFormat._
import net.jtownson.swakka.jsonschema.{JsonSchema, SchemaWriter}
import net.jtownson.swakka.misc.jsObject
import shapeless.{::, HList, HNil}
import spray.json.{JsArray, JsObject, JsString, JsValue}

trait ResponsesJsonProtocol {

  implicit val hNilResponseFormat: ResponseJsonFormat[HNil] =
    _ => JsObject()


  implicit def hConsResponseFormat[H, T <: HList](implicit head: ResponseJsonFormat[H], tail: ResponseJsonFormat[T]): ResponseJsonFormat[H :: T] =
    func2Format((l: H :: T) => {
      flattenToObject(JsArray(head.write(l.head), tail.write(l.tail)))
    })


  implicit def responseFormat[T: SchemaWriter]: ResponseJsonFormat[ResponseValue[T]] =
  func2Format((rv: ResponseValue[T]) => swaggerResponse(rv.responseCode, rv.description, JsonSchema[T]()))


  private def swaggerResponse[T](status: Int, description: String, schema: JsonSchema[T])
                                (implicit sw: SchemaWriter[T]): JsValue =
    JsObject(
      String.valueOf(status) -> jsObject(
        Some("description" -> JsString(description)),
        Some("schema" -> sw.write(schema))
      )
    )
}

object ResponsesJsonProtocol extends ResponsesJsonProtocol