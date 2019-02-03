package net.jtownson.swakka.jsonschema

import akka.http.scaladsl.model.DateTime
import net.jtownson.swakka.jsonschema.SchemaWriter.instance

trait DateSchemaWriters {

  implicit val akkaHttpDateSchemaWriter: SchemaWriter[DateTime] =
    instance((schema: JsonSchema[DateTime]) => Schemas.dateSchema(schema.description))
}

object DateSchemaWriters extends DateSchemaWriters
