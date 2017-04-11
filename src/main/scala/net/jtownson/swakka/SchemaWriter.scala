package net.jtownson.swakka

import net.jtownson.swakka.ApiModelDictionary.apiModelDictionary
import net.jtownson.swakka.FieldnameExtractor.fieldNames
import net.jtownson.swakka.OpenApiModel.ResponseValue
import spray.json.{JsObject, JsString, JsValue}
import net.jtownson.swakka.JsonSchemaJsonProtocol._
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

  private def numberSchema(description: Option[String]) =
    jsObject(
      Some("type" -> JsString("number")),
      description.map("description" -> JsString(_))
    )

  private def objectSchema(description: Option[String], fieldSchemas: List[(String, JsValue)]) =
    jsObject(
      Some("type" -> JsString("object")),
      description.map("description" -> JsString(_)),
      Some("properties" -> JsObject(fieldSchemas: _*))
    )

  private def jsObject(fields: Option[(String, JsValue)]*): JsObject =
    JsObject(fields.flatten: _*)

  implicit val unitWriter: SchemaWriter[Unit] =
    (_: JsonSchema[Unit]) => unitSchema

  implicit val stringWriter: SchemaWriter[String] =
    (s: JsonSchema[String]) => stringSchema(s.description)

  implicit def numberWriter[T: Numeric]: SchemaWriter[T] =
    (s: JsonSchema[T]) => numberSchema(s.description)

  implicit def schemaWriter[T <: Product : TypeTag](constructor: () => T): SchemaWriter[T] =
    (s: JsonSchema[T]) => {
      objectSchema(s.description, Nil)
    }

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

  implicit def responseValueWriter[T](implicit ev: SchemaWriter[T]): SchemaWriter[ResponseValue[T]] =
    (_: JsonSchema[ResponseValue[T]]) => ev.write(JsonSchema[T]())

  private def writeSchema[T: SchemaWriter](description: Option[String]): JsValue = {
    jsonSchemaJsonWriter[T].write(JsonSchema[T](description))
  }
}