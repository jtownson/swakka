package net.jtownson.swakka.jsonschema

import akka.http.scaladsl.model.DateTime

trait DateSchemaWriters {

  val akkaHttpDateSchemaWriter: SchemaWriter[DateTime] =
    (schema: JsonSchema[DateTime]) => Schemas.dateSchema(schema.description)
}

object DateSchemaWriters extends DateSchemaWriters
