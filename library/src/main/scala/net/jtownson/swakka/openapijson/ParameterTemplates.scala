package net.jtownson.swakka.openapijson

import net.jtownson.swakka.jsonschema.{JsonSchema, SchemaWriter}
import net.jtownson.swakka.misc.jsObject
import net.jtownson.swakka.openapimodel._
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

  def constrainedParam(name: Symbol,
                       in: String,
                       description: Option[String],
                       required: Boolean,
                       `type`: String,
                       format: Option[String],
                       default: Option[JsValue] = None,
                       items: Option[JsValue] = None,
                       maximum: Option[JsValue] = None,
                       exclusiveMaximum: Option[JsValue] = None,
                       minimum: Option[JsValue] = None,
                       exclusiveMinimum: Option[JsValue] = None,
                       minLength: Option[JsValue] = None,
                       maxLength: Option[JsValue] = None,
                       pattern: Option[JsValue] = None,
                       maxItems: Option[JsValue] = None,
                       minItems: Option[JsValue] = None,
                       uniqueItems: Option[JsValue] = None,
                       enum: Option[JsValue] = None,
                       multipleOf: Option[JsValue] = None): JsValue =
    jsObject(
      Some("name" -> JsString(name.name)),
      Some("in" -> JsString(in)),
      description.map("description" -> JsString(_)),
      Some("required" -> JsBoolean(required)),
      Some("type" -> JsString(`type`)),
      format.map("format" -> JsString(_)),
      default.map("default" -> _),
      items.map("items" -> _),
      maximum.map("maximum" -> _),
      exclusiveMaximum.map("exclusiveMaximum" -> _),
      minimum.map("minimum" -> _),
      exclusiveMinimum.map("exclusiveMinimum" -> _),
      minLength.map("minLength" -> _),
      maxLength.map("maxLength" -> _),
      pattern.map("pattern" -> _),
      maxItems.map("maxItems" -> _),
      minItems.map("minItems" -> _),
      uniqueItems.map("uniqueItems" -> _),
      enum.map("enum" -> _),
      multipleOf.map("multipleOf" -> _)
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

  def enumOf[T: JsonFormat](
      param: PathParameterConstrained[T, _]): Option[JsValue] =
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

  def enumOf[T: JsonFormat](param: HeaderParameter[T]): Option[JsValue] =
    param.enum.map(_.toJson)

}
