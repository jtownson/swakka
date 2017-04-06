package net.jtownson.minimal

import spray.json.{DefaultJsonProtocol, JsValue, JsonFormat, JsonWriter}

trait MinimalJsonSchemaJsonProtocol extends DefaultJsonProtocol {

  def jsonSchemaJsonWriter[T](implicit ev: SchemaWriter[T]) = new JsonWriter[MinimalJsonSchema[T]] {
    override def write(schema: MinimalJsonSchema[T]): JsValue = ev.write(schema)
  }

  implicit def jsonSchemaJsonFormat[T](implicit ev: SchemaWriter[T]): JsonFormat[MinimalJsonSchema[T]] =
    lift(jsonSchemaJsonWriter[T])

}

object MinimalJsonSchemaJsonProtocol extends MinimalJsonSchemaJsonProtocol