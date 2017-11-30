package net.jtownson.swakka.jsonschema

trait SchemaWriters extends BasicSchemaWriters with HListSchemaWriters with EnumSchemaWriters

object SchemaWriters extends SchemaWriters
