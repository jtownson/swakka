package net.jtownson.swakka.jsonprotocol

import net.jtownson.swakka.jsonschema.ApiModelDictionary._
import net.jtownson.swakka.jsonschema.{JsonSchema, SchemaWriter}
import shapeless.{::, HList, HNil}
import spray.json.{JsArray, JsBoolean, JsFalse, JsObject, JsString, JsValue}
import ParameterJsonFormat.func2Format
import net.jtownson.swakka.model.Parameters.{BodyParameter, PathParameter, QueryParameter}

import scala.reflect.runtime.universe.TypeTag

trait ParametersJsonProtocol {

  implicit val strQueryParamFormat: ParameterJsonFormat[QueryParameter[String]] =
    (qp: QueryParameter[String]) => simpleParam(qp.name, "query", qp.description, qp.required, JsString("string"), None)

  implicit val floatQueryParamFormat: ParameterJsonFormat[QueryParameter[Float]] =
    (qp: QueryParameter[Float]) => simpleParam(qp.name, "query", qp.description, qp.required, JsString("number"), Some("float"))

  implicit val doubleQueryParamFormat: ParameterJsonFormat[QueryParameter[Double]] =
    (qp: QueryParameter[Double]) => simpleParam(qp.name, "query", qp.description, qp.required, JsString("number"), Some("double"))

  implicit val booleanQueryParamFormat: ParameterJsonFormat[QueryParameter[Boolean]] =
    (qp: QueryParameter[Boolean]) => simpleParam(qp.name, "query", qp.description, qp.required, JsString("boolean"), None)

  implicit val intQueryParamFormat: ParameterJsonFormat[QueryParameter[Int]] =
    (qp: QueryParameter[Int]) => simpleParam(qp.name, "query", qp.description, qp.required, JsString("integer"), Some("int32"))

  implicit val longQueryParamFormat: ParameterJsonFormat[QueryParameter[Long]] =
    (qp: QueryParameter[Long]) => simpleParam(qp.name, "query", qp.description, qp.required, JsString("integer"), Some("int64"))


  implicit val strPathParamFormat: ParameterJsonFormat[PathParameter[String]] =
    (pp: PathParameter[String]) => simpleParam(pp.name, "path", pp.description, pp.required, JsString("string"), None)

  implicit val floatPathParamFormat: ParameterJsonFormat[PathParameter[Float]] =
    (pp: PathParameter[Float]) => simpleParam(pp.name, "path", pp.description, pp.required, JsString("number"), Some("float"))

  implicit val doublePathParamFormat: ParameterJsonFormat[PathParameter[Double]] =
    (pp: PathParameter[Double]) => simpleParam(pp.name, "path", pp.description, pp.required, JsString("number"), Some("double"))

  implicit val booleanPathParamFormat: ParameterJsonFormat[PathParameter[Boolean]] =
    (pp: PathParameter[Boolean]) => simpleParam(pp.name, "path", pp.description, pp.required, JsString("boolean"), None)

  implicit val intPathParamFormat: ParameterJsonFormat[PathParameter[Int]] =
    (pp: PathParameter[Int]) => simpleParam(pp.name, "path", pp.description, pp.required, JsString("integer"), Some("int32"))

  implicit val longPathParamFormat: ParameterJsonFormat[PathParameter[Long]] =
    (pp: PathParameter[Long]) => simpleParam(pp.name, "path", pp.description, pp.required, JsString("integer"), Some("int64"))


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

  private def simpleParam(name: Symbol, in: String, description: Option[String], required: Boolean, `type`: JsValue, format: Option[String]): JsValue =
    JsObject(List(
      Some("name" -> JsString(name.name)),
      Some("in" -> JsString(in)),
      description.map("description" -> JsString(_)),
      Some("required" -> JsBoolean(required)),
      Some("type" -> `type`),
      format.map("format" -> JsString(_))).flatten: _*
    )

}

object ParametersJsonProtocol extends ParametersJsonProtocol