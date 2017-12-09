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

import net.jtownson.swakka.jsonschema.Schemas._
import net.jtownson.swakka.openapimodel._

trait BasicSchemaWriters {

  implicit val unitWriter: SchemaWriter[Unit] =
    (_: JsonSchema[Unit]) => unitSchema

  implicit val stringWriter: SchemaWriter[String] =
    (s: JsonSchema[String]) => stringSchema(s.description)

//  implicit def booleanWriter: SchemaWriter[Boolean] =
//    (s: JsonSchema[Boolean]) => booleanSchema(s.description)

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

  implicit def mapWriter[K, V](implicit ev: SchemaWriter[K]): SchemaWriter[Map[K, V]] =
    (s: JsonSchema[Map[K, V]]) => mapSchema(s.description, ev.write(JsonSchema[K]()))

  implicit def responseValueWriter[T, Headers](implicit ev: SchemaWriter[T]):
  SchemaWriter[ResponseValue[T, Headers]] =
    (_: JsonSchema[ResponseValue[T, Headers]]) => ev.write(JsonSchema[T]())

}
