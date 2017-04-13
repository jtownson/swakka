package net.jtownson.swakka

import net.jtownson.swakka.OpenApiModel.{BodyParameter, PathParameter, QueryParameter}
import shapeless.{::, HList, HNil}
import spray.json.{DefaultJsonProtocol, JsArray, JsBoolean, JsFalse, JsObject, JsString, JsValue}
import ParameterJsonFormat._
import net.jtownson.swakka.ApiModelDictionary._
import scala.reflect.runtime.universe.TypeTag

object ParametersJsonProtocol extends DefaultJsonProtocol {


  implicit val strParamFormat: ParameterJsonFormat[QueryParameter[String]] =
    (qp: QueryParameter[String]) => simpleParam(qp.name, "query", "", false, JsString("string"))

  implicit val intParamFormat: ParameterJsonFormat[QueryParameter[Int]] =
    (qp: QueryParameter[Int]) => simpleParam(qp.name, "query", "", false, JsString("integer"))

  implicit val strPathParamFormat: ParameterJsonFormat[PathParameter[String]] =
    (pp: PathParameter[String]) => simpleParam(pp.name, "path", "", false, JsString("string"))

  implicit def bodyParamFormat[T: TypeTag](implicit ev: SchemaWriter[T]): ParameterJsonFormat[BodyParameter[T]] = {

    implicit val dict = apiModelDictionary[T]

    func2Format((bp: BodyParameter[T]) => JsObject(
      "name" -> JsString(bp.name.name),
      "in" -> JsString("body"),
      "description" -> JsString(""),
      "required" -> JsFalse,
      "schema" -> ev.write(JsonSchema[T]())
    ))
  }

  implicit val hNilParamFormat: ParameterJsonFormat[HNil] =
    _ => JsArray()

  implicit def hConsParamFormat[H, T <: HList](implicit head: ParameterJsonFormat[H], tail: ParameterJsonFormat[T]): ParameterJsonFormat[H :: T] =
    func2Format((l: H :: T) => {
      Flattener.flattenToArray(JsArray(head.write(l.head), tail.write(l.tail)))
    })

  private def simpleParam(name: Symbol, in: String, description: String, required: Boolean, `type`: JsValue): JsValue =
    JsObject(
      "name" -> JsString(name.name),
      "in" -> JsString(in),
      "description" -> JsString(description),
      "required" -> JsBoolean(required),
      "type" -> `type`
    )
}
