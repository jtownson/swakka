package net.jtownson.swakka.jsonschema

import net.jtownson.swakka.jsonschema.SchemaWriter.instance

trait EnumSchemaWriters {
  implicit def enumSchemaWriter[T <: scala.Enumeration](implicit enu: T): SchemaWriter[T#Value] =
    instance((schema: JsonSchema[T#Value]) => Schemas.enumSchema(schema.description, enu.values.toList))
}

object EnumSchemaWriters extends EnumSchemaWriters