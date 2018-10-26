package net.jtownson.swakka.jsonschema

trait EnumeratumSchemaWriters {

  implicit def enumeratumSchemaWriter[T <: enumeratum.EnumEntry](
      implicit enu: enumeratum.Enum[T]): SchemaWriter[enumeratum.Enum[T]] =
    (schema: JsonSchema[enumeratum.Enum[T]]) => {
      Schemas.enumSchema(schema.description, enu.values)
    }
}

object EnumeratumSchemaWriters extends EnumeratumSchemaWriters
