package net.jtownson.swakka

import net.jtownson.swakka.Flattener.flattenToObject
import net.jtownson.swakka.OpenApiModel.ResponseValue
import net.jtownson.swakka.ResponseJsonFormat._
import shapeless.{::, HList, HNil}
import spray.json.{DefaultJsonProtocol, JsArray, JsObject, JsValue}

trait ResponsesJsonProtocol extends DefaultJsonProtocol {

  implicit val strResponseFormat: ResponseJsonFormat[ResponseValue[String]] =
    (rv: ResponseValue[String]) => swaggerResponse(rv.responseCode, MinimalJsonSchema[String]())


  implicit val hNilResponseFormat: ResponseJsonFormat[HNil] =
    _ => JsArray()


  implicit def hConsResponseFormat[H, T <: HList](implicit head: ResponseJsonFormat[H], tail: ResponseJsonFormat[T]): ResponseJsonFormat[H :: T] =
    func2Format((l: H :: T) => {
      flattenToObject(JsArray(head.write(l.head), tail.write(l.tail)))
    })


  import scala.reflect.runtime.universe._

  implicit def caseClassResponseFormat0[T <: Product: TypeTag : SchemaWriter](constructor: () => T):
  ResponseJsonFormat[ResponseValue[T]] =
    caseClassFormat[T]

  implicit def caseClassResponseFormat1[T <: Product: TypeTag : SchemaWriter,
                                        F1: SchemaWriter](constructor: (F1) => T):
  ResponseJsonFormat[ResponseValue[T]] =
    caseClassFormat[T]


  private def caseClassFormat[T <: Product : TypeTag : SchemaWriter]: ResponseJsonFormat[ResponseValue[T]] =
    func2Format((rv: ResponseValue[T]) => swaggerResponse(rv.responseCode, MinimalJsonSchema[T]()))

  private def swaggerResponse[T](status: Int, schema: MinimalJsonSchema[T])
                                (implicit sw: SchemaWriter[T]): JsValue =
    JsObject(
      String.valueOf(status) -> JsObject(
        "schema" -> sw.write(schema)
      )
    )
}

object ResponsesJsonProtocol extends ResponsesJsonProtocol