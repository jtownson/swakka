package net.jtownson.swakka.jsonschema

import net.jtownson.swakka.jsonschema.Schemas.{arraySchema, numericSchema, stringSchema, unitSchema}
import net.jtownson.swakka.model.Responses.ResponseValue
import shapeless.HNil
import spray.json.JsNull

trait BasicSchemaWriters {

  implicit val unitWriter: SchemaWriter[Unit] =
    (_: JsonSchema[Unit]) => unitSchema

  implicit val stringWriter: SchemaWriter[String] =
    (s: JsonSchema[String]) => stringSchema(s.description)

  implicit def intWriter: SchemaWriter[Int] =
    (s: JsonSchema[Int]) => numericSchema(s.description, "integer", Some("int32"))

  implicit def longWriter: SchemaWriter[Long] =
    (s: JsonSchema[Long]) => numericSchema(s.description, "integer", Some("int64"))

  implicit def floatWriter: SchemaWriter[Float] =
    (s: JsonSchema[Float]) => numericSchema(s.description, "number", Some("float"))

  implicit def doubleWriter: SchemaWriter[Double] =
    (s: JsonSchema[Double]) => numericSchema(s.description, "number", Some("double"))

  implicit def optionWriter[T](implicit ev: SchemaWriter[T]): SchemaWriter[Option[T]] =
    (s: JsonSchema[Option[T]]) => ev.write(JsonSchema[T](s.description))

  implicit def seqWriter[T](implicit ev: SchemaWriter[T]): SchemaWriter[Seq[T]] =
    (s: JsonSchema[Seq[T]]) => arraySchema(s.description, ev.write(JsonSchema[T]()))

  implicit def responseValueWriter[T, Headers](implicit ev: SchemaWriter[T]):
  SchemaWriter[ResponseValue[T, Headers]] =
    (_: JsonSchema[ResponseValue[T, Headers]]) => ev.write(JsonSchema[T]())

  implicit val hNilSchemaWriter: SchemaWriter[HNil] =
    _ => JsNull

}
