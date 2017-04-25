package net.jtownson.swakka.jsonprotocol

import net.jtownson.swakka.jsonschema.ApiModelDictionary._
import net.jtownson.swakka.OpenApiModel.{BodyParameter, PathParameter, QueryParameter}
import net.jtownson.swakka.jsonschema.{JsonSchema, SchemaWriter}
import shapeless.{::, HList, HNil}
import spray.json.{JsArray, JsBoolean, JsFalse, JsObject, JsString, JsValue}
import ParameterJsonFormat.func2Format

import scala.reflect.runtime.universe.TypeTag

trait ParametersJsonProtocol {

  implicit val strParamFormat: ParameterJsonFormat[QueryParameter[String]] =
    (qp: QueryParameter[String]) => simpleParam(qp.name, "query", qp.description, qp.required, JsString("string"))

  implicit val intParamFormat: ParameterJsonFormat[QueryParameter[Int]] =
    (qp: QueryParameter[Int]) => simpleParam(qp.name, "query", qp.description, qp.required, JsString("integer"))

  implicit val strPathParamFormat: ParameterJsonFormat[PathParameter[String]] =
    (pp: PathParameter[String]) => simpleParam(pp.name, "path", pp.description, false, JsString("string"))

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

  private def simpleParam(name: Symbol, in: String, description: Option[String], required: Boolean, `type`: JsValue): JsValue =
    JsObject(List(
      Some("name" -> JsString(name.name)),
      Some("in" -> JsString(in)),
      description.map("description" -> JsString(_)),
      Some("required" -> JsBoolean(required)),
      Some("type" -> `type`)).flatten: _*
    )

}

object ParametersJsonProtocol extends ParametersJsonProtocol