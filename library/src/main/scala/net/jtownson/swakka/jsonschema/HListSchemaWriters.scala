/*
 * Copyright 2017 Jeremy Townson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.jtownson.swakka.jsonschema

import net.jtownson.swakka.jsonschema.FieldNameExtractor.nonOptional
import net.jtownson.swakka.jsonschema.SchemaWriter.instance
import net.jtownson.swakka.jsonschema.Schemas.objectSchema
import shapeless.labelled.FieldType
import shapeless.ops.hlist.RightFolder.Aux
import shapeless.ops.hlist.ToTraversable
import shapeless.ops.record.Keys
import shapeless.{::, HList, HNil, LabelledGeneric, Lazy, Witness}
import spray.json.{JsNull, JsObject, JsValue}
import spray.json.DefaultJsonProtocol._

trait HListSchemaWriters {

  implicit val hNilSchemaWriter: SchemaWriter[HNil] =
    instance(_  => JsNull)

  implicit def hlistObjectEncoder[K <: Symbol, H, T <: HList](
      implicit
      witness: Witness.Aux[K],
      hWriter: Lazy[SchemaWriter[H]],
      tWriter: SchemaWriter[T]
  ): SchemaWriter[FieldType[K, H] :: T] = {

    val f: JsonSchema[FieldType[K, H] :: T] => JsValue = _ => {

      val fieldName: String = witness.value.name

      val hSchemaJs: JsObject = asJsObject(hWriter.value.write(JsonSchema[H]()))
      val tSchemaJs: JsObject = asJsObject(tWriter.write(JsonSchema[T]()))

      val hSchemaField = (fieldName, hSchemaJs)
      val tSchemaFields = tSchemaJs.fields

      val fields = tSchemaFields.+(hSchemaField).toList

      JsObject(fields: _*)
    }

    instance(f)
  }

  private val fieldDocJsonFormat = jsonFormat1(FieldDoc)

  implicit def genericObjectEncoder[A <: Product,
                                    L <: HList,
                                    R <: HList,
                                    O <: HList](
      implicit
      generic: LabelledGeneric.Aux[A, L],
      lEncoder: Lazy[SchemaWriter[L]],
      folder: Aux[L, HNil.type, nonOptional.type, R],
      keys: Keys.Aux[R, O],
      traversable: ToTraversable.Aux[O, List, Symbol],
      classDoc: ClassDoc[A]
  ): SchemaWriter[A] = {
    val f: JsonSchema[A] => JsValue =
      schema => {

        val classDocJs: Map[String, JsValue] = classDoc.entries.mapValues(fieldDocJsonFormat.write)

        val fieldSchemas: Map[String, JsValue] =
          asJsObject(lEncoder.value.write(JsonSchema[L]())).fields

        val annotatedFieldSchemas: Map[String, JsValue] = fieldSchemas map {
          case (field, schemaJs) => {
            val annotationSchemaJs = classDocJs.getOrElse(field, JsObject())
            val annotationOverrides = asJsObject(annotationSchemaJs).fields ++ asJsObject(schemaJs).fields

            (field, JsObject(annotationOverrides))
          }
        }

        val requiredFields = FieldNameExtractor[A].extract(nonOptional)

        objectSchema(schema.description, requiredFields, annotatedFieldSchemas.toList)
      }
    instance(f)
  }

  private def asJsObject(jsValue: JsValue): JsObject = jsValue match {
    case obj: JsObject => obj
    case _             => JsObject()
  }
}
