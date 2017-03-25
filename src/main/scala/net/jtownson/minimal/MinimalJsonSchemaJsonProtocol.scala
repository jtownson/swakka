package net.jtownson.minimal

import net.jtownson.minimal.FieldnameExtractor.fieldNames
import spray.json.{DefaultJsonProtocol, JsObject, JsString, JsValue, JsonFormat, JsonWriter}

trait SchemaWriter[T] {
  def write(schema: MinimalJsonSchema[T]): JsValue
}

class MinimalJsonSchemaJsonProtocol[T](implicit ev: SchemaWriter[T]) extends DefaultJsonProtocol {

  val jsonSchemaJsonWriter = new JsonWriter[MinimalJsonSchema[T]] {
    override def write(schema: MinimalJsonSchema[T]): JsValue = ev.write(schema)
  }

  implicit val jsonSchemaJsonFormat: JsonFormat[MinimalJsonSchema[T]] =
    lift(jsonSchemaJsonWriter)

}

object MinimalJsonSchemaJsonProtocol {

  private val unitSchema = JsObject()

  private val stringSchema = JsObject("type" -> JsString("string"))

  private val numberSchema = JsObject("type" -> JsString("number"))

  private def objectSchema(fieldSchemas: List[(String, JsValue)]) = JsObject(
    "type" -> JsString("object"),
    "properties" -> JsObject(
      fieldSchemas: _*
    ))

  implicit val unitWriter: SchemaWriter[Unit] =
    (_: MinimalJsonSchema[Unit]) => unitSchema

  implicit val stringWriter: SchemaWriter[String] =
    (_: MinimalJsonSchema[String]) => stringSchema

  implicit def numberWriter[T : Numeric]: SchemaWriter[T] =
    (_: MinimalJsonSchema[T]) => numberSchema

  import scala.reflect.runtime.universe._

  implicit def schemaWriter[T <: Product: TypeTag](constructor: () => T): SchemaWriter[T] =
    (_: MinimalJsonSchema[T]) => objectSchema(Nil)

  implicit def schemaWriter[T <: Product: TypeTag,
                            F1: SchemaWriter]
                            (constructor: (F1) => T): SchemaWriter[T] =
    (_: MinimalJsonSchema[T]) => {

      val fields: List[String] = fieldNames[T]

      objectSchema(List(
        fields(0) -> writeSchema[F1]))
    }

  implicit def schemaWriter[T <: Product: TypeTag,
                            F1: SchemaWriter,
                            F2: SchemaWriter]
                            (constructor: (F1, F2) => T): SchemaWriter[T] =
    (_: MinimalJsonSchema[T]) => {

      val fields: List[String] = fieldNames[T]

      objectSchema(List(
        fields(0) -> writeSchema[F1],
        fields(1) -> writeSchema[F2]))
    }

  def writeSchema[T: SchemaWriter]: JsValue = {
    val schema = MinimalJsonSchema[T]()
    val jsonProtocol = new MinimalJsonSchemaJsonProtocol[T]
    jsonProtocol.jsonSchemaJsonWriter.write(schema)
  }
}