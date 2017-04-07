package net.jtownson.swakka

import net.jtownson.swakka.OpenApiModel.QueryParameter
import shapeless.{::, HList, HNil}
import spray.json.{DefaultJsonProtocol, JsArray, JsBoolean, JsObject, JsString, JsValue, JsonFormat, JsonWriter}
import ParameterJsonFormat._

object ParametersJsonProtocol extends DefaultJsonProtocol {


  implicit val strParamFormat: ParameterJsonFormat[QueryParameter[String]] =
    (qp: QueryParameter[String]) => swaggerParam(qp.name, "query", "", false, JsString("string"))

  implicit val intParamFormat: ParameterJsonFormat[QueryParameter[Int]] =
    (qp: QueryParameter[Int]) => swaggerParam(qp.name, "query", "", false, JsString("integer"))

  val hNilParamWriter: ParameterJsonFormat[HNil] =
    _ => JsArray()

  implicit val hNilParamFormat: ParameterJsonFormat[HNil] =
    _ => JsArray()

  implicit def hConsParamFormat[H, T <: HList](implicit head: ParameterJsonFormat[H], tail: ParameterJsonFormat[T]): ParameterJsonFormat[H :: T] =
    func2Format((l: H :: T) => {
      Flattener.flattenToArray(JsArray(head.write(l.head), tail.write(l.tail)))
    })

  private def swaggerParam(name: Symbol, in: String, description: String, required: Boolean, `type`: JsValue): JsValue =
    JsObject(
      "name" -> JsString(name.name),
      "in" -> JsString(in),
      "description" -> JsString(description),
      "required" -> JsBoolean(required),
      "type" -> `type`
    )
}
