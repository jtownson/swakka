package net.jtownson.swakka.jsonschema

trait SchemaWriters
    extends BasicSchemaWriters
    with HListSchemaWriters
    with EnumSchemaWriters
    with EnumeratumSchemaWriters
    with DateSchemaWriters

object SchemaWriters extends SchemaWriters
