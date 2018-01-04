package net.jtownson.swakka.openapijson

import net.jtownson.swakka.jsonschema.{JsonSchema, SchemaWriter}
import net.jtownson.swakka.misc.jsObject
import net.jtownson.swakka.openapimodel.{HeaderParameter, Parameter, PathParameter, QueryParameter}
import spray.json.{JsBoolean, JsString, JsValue, JsonFormat}
import spray.json._

object ParameterTemplates {

  def simpleParam(name: Symbol,
                  in: String,
                  description: Option[String],
                  required: Boolean,
                  `type`: String,
                  format: Option[String],
                  default: Option[JsValue] = None,
                  enum: Option[JsValue] = None): JsValue =
    jsObject(
      Some("name" -> JsString(name.name)),
      Some("in" -> JsString(in)),
      description.map("description" -> JsString(_)),
      Some("required" -> JsBoolean(required)),
      Some("type" -> JsString(`type`)),
      format.map("format" -> JsString(_)),
      default.map("default" -> _),
      enum.map("enum" -> _)
    )

  def bodyParam[T](ev: SchemaWriter[T],
                               name: Symbol,
                               description: Option[String],
                               required: Boolean,
                               default: Option[JsValue] = None,
                               enum: Option[JsValue] = None) = {
    jsObject(
      Some("name" -> JsString(name.name)),
      Some("in" -> JsString("body")),
      description.map("description" -> JsString(_)),
      Some("required" -> JsBoolean(required)),
      default.map("default" -> _),
      Some("schema" -> ev.write(JsonSchema[T]()))
    )
  }

  def enumOf[T: JsonFormat](param: PathParameter[T]): Option[JsValue] =
    param.enum.map(_.toJson)

  def enumOf[T: JsonFormat](param: QueryParameter[T]): Option[JsValue] =
    param.enum.map(_.toJson)

  def enumOfOption[T: JsonFormat](
      param: QueryParameter[Option[T]]): Option[JsValue] =
    param.enum.map(seq => seq.flatten).map(_.toJson)

  def defaultOf[T: JsonWriter](param: Parameter[Option[T]]): Option[JsValue] =
    param.default.flatten.map(_.toJson)

  def enumOfOption[T: JsonFormat](
      param: HeaderParameter[Option[T]]): Option[JsValue] =
    param.enum.map(seq => seq.flatten).map(_.toJson)

  def enumOf[T: JsonFormat](
      param: HeaderParameter[T]): Option[JsValue] =
    param.enum.map(_.toJson)

}
