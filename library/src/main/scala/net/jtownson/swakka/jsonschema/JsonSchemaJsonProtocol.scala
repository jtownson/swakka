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

import spray.json.{DefaultJsonProtocol, JsonFormat, JsonWriter}

trait JsonSchemaJsonProtocol extends DefaultJsonProtocol with SchemaWriters {

  implicit def jsonSchemaJsonWriter[T](implicit ev: SchemaWriter[T]): JsonWriter[JsonSchema[T]] =
    JsonWriter.func2Writer(t => ev.write(t))

  implicit def jsonSchemaJsonFormat[T](implicit ev: SchemaWriter[T]): JsonFormat[JsonSchema[T]] =
    lift(jsonSchemaJsonWriter[T])

}

object JsonSchemaJsonProtocol extends JsonSchemaJsonProtocol