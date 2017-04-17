package net.jtownson.swakka.jsonschema

import spray.json.{DefaultJsonProtocol, JsValue, JsonFormat, JsonWriter}

trait JsonSchemaJsonProtocol extends DefaultJsonProtocol {

  def jsonSchemaJsonWriter[T](implicit ev: SchemaWriter[T]) = new JsonWriter[JsonSchema[T]] {
    override def write(schema: JsonSchema[T]): JsValue = ev.write(schema)
  }

  implicit def jsonSchemaJsonFormat[T](implicit ev: SchemaWriter[T]): JsonFormat[JsonSchema[T]] =
    lift(jsonSchemaJsonWriter[T])

}

object JsonSchemaJsonProtocol extends JsonSchemaJsonProtocol