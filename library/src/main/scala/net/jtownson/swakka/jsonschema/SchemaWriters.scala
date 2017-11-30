package net.jtownson.swakka.jsonschema

trait SchemaWriters
    extends BasicSchemaWriters
    with HListSchemaWriters
    with EnumSchemaWriters
    with DateSchemaWriters

object SchemaWriters extends SchemaWriters
