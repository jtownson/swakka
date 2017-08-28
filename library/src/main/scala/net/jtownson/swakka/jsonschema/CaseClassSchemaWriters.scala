package net.jtownson.swakka.jsonschema

import net.jtownson.swakka.jsonschema.ApiModelDictionary.apiModelDictionary
import net.jtownson.swakka.jsonschema.JsonSchemaJsonProtocol.jsonSchemaJsonWriter
import net.jtownson.swakka.jsonschema.Schemas._
import net.jtownson.swakka.misc.FieldnameExtractor.fieldNames
import spray.json.JsValue

import scala.reflect.runtime.universe.TypeTag

trait CaseClassSchemaWriters {

  implicit def schemaWriter[T <: Product : TypeTag](constructor: () => T): SchemaWriter[T] =
    (s: JsonSchema[T]) => objectSchema(s.description, Nil, Nil)

  implicit def schemaWriter[T <: Product : TypeTag, F1: SchemaWriter]
  (constructor: (F1) => T): SchemaWriter[T] =
    (s: JsonSchema[T]) => {

      val tDictionary: Map[String, ApiModelPropertyEntry] = apiModelDictionary[T]

      val fields: Seq[String] = tDictionary.keys.toSeq

      val f1Entry = tDictionary(fields(0))

      objectSchema(
        s.description,
        requiredFields(tDictionary),
        List(fields(0) -> writeSchema[F1](f1Entry.value))
      )
    }

  implicit def schemaWriter[T <: Product : TypeTag,
  F1: SchemaWriter,
  F2: SchemaWriter]
  (constructor: (F1, F2) => T): SchemaWriter[T] =
    (s: JsonSchema[T]) => {

      val fields: List[String] = fieldNames[T]

      val tDictionary = apiModelDictionary[T]

      val f1Entry = tDictionary(fields(0))
      val f2Entry = tDictionary(fields(1))

      objectSchema(
        s.description,
        requiredFields(tDictionary),
        List(
          fields(0) -> writeSchema[F1](f1Entry.value),
          fields(1) -> writeSchema[F2](f2Entry.value))
      )
    }

  implicit def schemaWriter[T <: Product : TypeTag,
  F1: SchemaWriter,
  F2: SchemaWriter,
  F3: SchemaWriter]
  (constructor: (F1, F2, F3) => T): SchemaWriter[T] =
    (s: JsonSchema[T]) => {

      val fields: List[String] = fieldNames[T]

      val tDictionary = apiModelDictionary[T]

      val f1Entry: ApiModelPropertyEntry = tDictionary(fields(0))
      val f2Entry: ApiModelPropertyEntry = tDictionary(fields(1))
      val f3Entry: ApiModelPropertyEntry = tDictionary(fields(2))

      objectSchema(
        s.description,
        requiredFields(tDictionary),
        List(
          fields(0) -> writeSchema[F1](f1Entry.value),
          fields(1) -> writeSchema[F2](f2Entry.value),
          fields(2) -> writeSchema[F3](f3Entry.value)
        ))
    }

  private def requiredFields(tDictionary: Map[String, ApiModelPropertyEntry]): List[String] =
    tDictionary.
      filter({ case (_, modelEntry) => modelEntry.required }).
      keys.
      toList

  private def writeSchema[T: SchemaWriter](description: Option[String]): JsValue = {
    jsonSchemaJsonWriter[T].write(JsonSchema[T](description))
  }

}
