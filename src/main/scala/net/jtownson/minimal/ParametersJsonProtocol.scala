package net.jtownson.minimal

import net.jtownson.minimal.MinimalOpenApiModel.QueryParameter
import shapeless.{::, HList, HNil}
import spray.json.{DefaultJsonProtocol, JsBoolean, JsNull, JsObject, JsString, JsValue, JsonFormat, JsonWriter}

object ParametersJsonProtocol extends DefaultJsonProtocol {

  val strParamWriter: JsonWriter[QueryParameter[String]] =
    qp => swaggerParam(qp.name, "query", "", false, JsString("string"))

  implicit val strParamFormat: JsonFormat[QueryParameter[String]] = lift(strParamWriter)

  val intParamWriter: JsonWriter[QueryParameter[Int]] =
    qp => swaggerParam(qp.name, "query", "", false, JsString("number"))


  implicit val intParamFormat: JsonFormat[QueryParameter[Int]] = lift(intParamWriter)

  val hNillParamWriter: JsonWriter[HNil] =
    _ => JsNull

  implicit val hNillParamFormat: JsonFormat[HNil] = lift(hNillParamWriter)

  def hConsParamWriter[H, T <: HList](implicit head: JsonFormat[H], tail: JsonFormat[T]): JsonWriter[H :: T] =
    l => JsObject("head" -> head.write(l.head), "tail" -> tail.write(l.tail))

  implicit def hConsParamFormat[H, T <: HList](implicit head: JsonFormat[H], tail: JsonFormat[T]): JsonFormat[H :: T] = lift(hConsParamWriter[H, T])

  private def swaggerParam(name: Symbol, in: String, description: String, required: Boolean, `type`: JsValue): JsValue =
    JsObject(
      "name" -> JsString(name.toString()),
      "in" -> JsString(in),
      "description" -> JsString(description),
      "required" -> JsBoolean(required),
      "type" -> `type`
    )
}
