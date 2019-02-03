package net.jtownson.swakka.jsonschema

import net.jtownson.swakka.jsonschema.SchemaWriter.instance

trait EnumeratumSchemaWriters {

  implicit def enumeratumSchemaWriter[T <: enumeratum.EnumEntry](
      implicit enu: enumeratum.Enum[T]): SchemaWriter[enumeratum.Enum[T]] =
    instance((schema: JsonSchema[enumeratum.Enum[T]]) => {
      Schemas.enumSchema(schema.description, enu.values)
    })
}

object EnumeratumSchemaWriters extends EnumeratumSchemaWriters
