package net.jtownson.swakka.jsonschema

// A json schema describing a type T
case class JsonSchema[T](description: Option[String] = None)
