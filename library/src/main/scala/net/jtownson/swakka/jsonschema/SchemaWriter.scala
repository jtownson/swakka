package net.jtownson.swakka.jsonschema

import spray.json.JsValue


// To avoid ambiguities with json formats used
// to write request/response case classes, there
// is a separate trait for writing the case class
// schemas.

trait SchemaWriter[T] {
  def write(schema: JsonSchema[T]): JsValue
}

object SchemaWriter extends BasicSchemaWriters with CaseClassSchemaWriters