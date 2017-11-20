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

import io.swagger.annotations.ApiModelProperty
import net.jtownson.swakka.misc.AnnotationExtractor.constructorAnnotations
import net.jtownson.swakka.misc.FieldnameExtractor.fieldNameTypes

import scala.collection.immutable.ListMap
import scala.reflect.runtime.universe._

// Produce additional schema documentation entries
// The resulting map is from the fields of a class
// to the ApiModelProperty entries for that field.

object SwaggerAnnotationClassDoc {

  // Instance of the ClassDoc type class.
  implicit def apiDoc[T: TypeTag]: ClassDoc[T] = new ClassDoc[T] {
    override def entries: Map[String, FieldDoc] = annotationEntries[T]
  }

  private def annotationEntries[T: TypeTag]: Map[String, FieldDoc] = {
    val entriesUnordered =
      constructorAnnotations[T](classOf[ApiModelProperty]).map(kv => (kv._1, tuples2Property(kv._2)))

    val entriesOrdered =
      fieldNameTypes[T].
        filter { case (fieldName, _) => entriesUnordered.contains(fieldName) }.
        map { case (fieldName, _) => (fieldName, entriesUnordered(fieldName))}

    ListMap(entriesOrdered: _*)
  }

  private def tuples2Property(s: Set[(String, String)]): FieldDoc = {

    val value: Option[String] = s.find(findField("value")).map(_._2)

    FieldDoc(value)
  }

  private def findField(field: String): ((String, String)) => Boolean = {
    case (f, _) if f == field => true
    case _ => false
  }
}
