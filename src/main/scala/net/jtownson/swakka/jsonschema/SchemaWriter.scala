package net.jtownson.swakka.jsonschema

import ApiModelDictionary.apiModelDictionary
import net.jtownson.swakka.misc.FieldnameExtractor.fieldNames
import net.jtownson.swakka.OpenApiModel.ResponseValue
import net.jtownson.swakka.jsonschema.JsonSchemaJsonProtocol._
import net.jtownson.swakka.misc.jsObject
import spray.json.{JsObject, JsString, JsValue}

import scala.reflect.runtime.universe.TypeTag

// To avoid ambiguities with json formats used
// to write request/response case classes, there
// is a separate trait for writing the case class
// schemas.

trait SchemaWriter[T] {
  def write(schema: JsonSchema[T]): JsValue
}

object SchemaWriter {

  private val unitSchema = JsObject()

  private def stringSchema(description: Option[String]) =
    jsObject(
      Some("type" -> JsString("string")),
      description.map("description" -> JsString(_))
    )

  private def numericSchema(description: Option[String], `type`: String, format: Option[String]) =
    jsObject(
      Some("type" -> JsString(`type`)),
      description.map("description" -> JsString(_)),
      format.map("format" -> JsString(_))
    )

  private def objectSchema(description: Option[String], fieldSchemas: List[(String, JsValue)]) =
    jsObject(
      Some("type" -> JsString("object")),
      description.map("description" -> JsString(_)),
      Some("properties" -> JsObject(fieldSchemas: _*))
    )

  private def arraySchema(description: Option[String], itemSchema: JsValue) =
    jsObject(
      Some("type", JsString("array")),
      description.map("description" -> JsString(_)),
      Some("items" -> itemSchema)
    )

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

  implicit def schemaWriter[T <: Product : TypeTag](constructor: () => T): SchemaWriter[T] =
    (s: JsonSchema[T]) => objectSchema(s.description, Nil)

  implicit def schemaWriter[T <: Product : TypeTag,
  F1: SchemaWriter : TypeTag]
  (constructor: (F1) => T): SchemaWriter[T] =
    (s: JsonSchema[T]) => {

      val fields: List[String] = fieldNames[T]

      val tDictionary = apiModelDictionary[T]

      objectSchema(s.description, List(
        fields(0) -> writeSchema[F1](tDictionary.get(fields(0)).map(_.value))))
    }

  implicit def schemaWriter[T <: Product : TypeTag,
  F1: SchemaWriter,
  F2: SchemaWriter]
  (constructor: (F1, F2) => T): SchemaWriter[T] =
    (s: JsonSchema[T]) => {

      val fields: List[String] = fieldNames[T]

      val tDictionary = apiModelDictionary[T]

      objectSchema(s.description, List(
        fields(0) -> writeSchema[F1](tDictionary.get(fields(0)).map(_.value)),
        fields(1) -> writeSchema[F2](tDictionary.get(fields(1)).map(_.value))
      ))
    }

  implicit def schemaWriter[T <: Product : TypeTag,
  F1: SchemaWriter,
  F2: SchemaWriter,
  F3: SchemaWriter]
  (constructor: (F1, F2, F3) => T): SchemaWriter[T] =
    (s: JsonSchema[T]) => {

      val fields: List[String] = fieldNames[T]

      val tDictionary = apiModelDictionary[T]

      objectSchema(s.description, List(
        fields(0) -> writeSchema[F1](tDictionary.get(fields(0)).map(_.value)),
        fields(1) -> writeSchema[F2](tDictionary.get(fields(1)).map(_.value)),
        fields(2) -> writeSchema[F3](tDictionary.get(fields(2)).map(_.value))
      ))
    }

  implicit def responseValueWriter[T, Headers](implicit ev: SchemaWriter[T]): SchemaWriter[ResponseValue[T, Headers]] =
    (_: JsonSchema[ResponseValue[T, Headers]]) => ev.write(JsonSchema[T]())

  private def writeSchema[T: SchemaWriter](description: Option[String]): JsValue = {
    jsonSchemaJsonWriter[T].write(JsonSchema[T](description))
  }
}