package net.jtownson.swakka.jsonschema

import net.jtownson.swakka.misc.jsObject
import spray.json.{JsArray, JsObject, JsString, JsValue}

object Schemas {

  val unitSchema = JsObject()

  def stringSchema(description: Option[String]) =
    jsObject(
      Some("type" -> JsString("string")),
      description.map("description" -> JsString(_))
    )

  def numericSchema(description: Option[String], `type`: String, format: Option[String]) =
    jsObject(
      Some("type" -> JsString(`type`)),
      description.map("description" -> JsString(_)),
      format.map("format" -> JsString(_))
    )

  def arraySchema(description: Option[String], itemSchema: JsValue) =
    jsObject(
      Some("type", JsString("array")),
      description.map("description" -> JsString(_)),
      Some("items" -> itemSchema)
    )

  def objectSchema(description: Option[String], requiredFields: List[String], fieldSchemas: List[(String, JsValue)]) = {
    jsObject(
      Some("type" -> JsString("object")),
      description.map("description" -> JsString(_)),
      optionalJsArray(requiredFields).map("required" -> _),
      Some("properties" -> JsObject(fieldSchemas: _*))
    )
  }

  private def optionalJsArray(requiredFields: List[String]): Option[JsArray] =
    optionally(requiredFields).map(requiredFields => requiredFields.map(JsString(_))).map(JsArray(_: _*))

  private def optionally[T](l: List[T]): Option[List[T]] = l match {
    case Nil => None
    case _ => Some(l)
  }
}
