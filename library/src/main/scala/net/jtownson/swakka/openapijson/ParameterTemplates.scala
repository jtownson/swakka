package net.jtownson.swakka.openapijson

import net.jtownson.swakka.misc.jsObject
import net.jtownson.swakka.openapimodel.PathParameter
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


  def enumOf[T: JsonFormat](param: PathParameter[T]): Option[JsValue] =
    param.enum.map(_.toJson)

}
