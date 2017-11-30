package net.jtownson.swakka.jsonschema

import net.jtownson.swakka.misc.jsObject
import spray.json.{JsArray, JsObject, JsString}

trait EnumSchemaWriters {

  implicit def enumSchemaWriter[T <: scala.Enumeration](implicit enu: T): SchemaWriter[T#Value] =
    (schema: JsonSchema[T#Value]) => {
      enumSchema(schema.description, enu.values.toList)
    }

  private def enumSchema(description: Option[String], values: List[Any]): JsObject = jsObject(
    Some("type" -> JsString("string")),
    Some("enum" -> JsArray(values.map(value => JsString(value.toString)): _*)),
    description.map("description" -> JsString(_))
  )

}

object EnumSchemaWriters extends EnumSchemaWriters