package net.jtownson.swakka.jsonschema

trait EnumSchemaWriters {
  implicit def enumSchemaWriter[T <: scala.Enumeration](implicit enu: T): SchemaWriter[T#Value] =
    (schema: JsonSchema[T#Value]) => Schemas.enumSchema(schema.description, enu.values.toList)
}

object EnumSchemaWriters extends EnumSchemaWriters