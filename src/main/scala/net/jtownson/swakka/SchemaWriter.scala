package net.jtownson.swakka

import net.jtownson.swakka.FieldnameExtractor.fieldNames
import net.jtownson.swakka.OpenApiModel.ResponseValue
import spray.json.{JsObject, JsString, JsValue}
import net.jtownson.swakka.JsonSchemaJsonProtocol._


trait SchemaWriter[T] {
  def write(schema: JsonSchema[T]): JsValue
}

object SchemaWriter {

  private val unitSchema = JsObject()

  private val stringSchema = JsObject("type" -> JsString("string"))

  private val numberSchema = JsObject("type" -> JsString("number"))

  private def objectSchema(fieldSchemas: List[(String, JsValue)]) = JsObject(
    "type" -> JsString("object"),
    "properties" -> JsObject(
      fieldSchemas: _*
    ))

  implicit val unitWriter: SchemaWriter[Unit] =
    (_: JsonSchema[Unit]) => unitSchema

  implicit val stringWriter: SchemaWriter[String] =
    (_: JsonSchema[String]) => stringSchema

  implicit def numberWriter[T : Numeric]: SchemaWriter[T] =
    (_: JsonSchema[T]) => numberSchema

  import scala.reflect.runtime.universe._

  implicit def schemaWriter[T <: Product: TypeTag](constructor: () => T): SchemaWriter[T] =
    (_: JsonSchema[T]) => objectSchema(Nil)

  implicit def schemaWriter[T <: Product: TypeTag, F1: SchemaWriter]
  (constructor: (F1) => T): SchemaWriter[T] =
    (_: JsonSchema[T]) => {

      val fields: List[String] = fieldNames[T]

      objectSchema(List(
        fields(0) -> writeSchema[F1]))
    }

  implicit def schemaWriter[T <: Product: TypeTag,
  F1: SchemaWriter,
  F2: SchemaWriter]
  (constructor: (F1, F2) => T): SchemaWriter[T] =
    (_: JsonSchema[T]) => {

      val fields: List[String] = fieldNames[T]

      objectSchema(List(
        fields(0) -> writeSchema[F1],
        fields(1) -> writeSchema[F2]))
    }

  implicit def responseValueWriter[T](implicit ev: SchemaWriter[T]): SchemaWriter[ResponseValue[T]] =
    (_: JsonSchema[ResponseValue[T]]) => ev.write(JsonSchema[T]())

  private def writeSchema[T: SchemaWriter]: JsValue = {
    val schema = JsonSchema[T]()
    jsonSchemaJsonWriter[T].write(schema)
  }
}