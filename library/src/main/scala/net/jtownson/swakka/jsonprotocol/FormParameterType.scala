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

package net.jtownson.swakka.jsonprotocol

import java.io.File

sealed trait FormParameterType[T] {
  def swaggerType: String

  def swaggerFormat: Option[String]
}

object FormParameterType {
  implicit val stringFormParam = new FormParameterType[String] {
    override def swaggerType: String = "string"

    override def swaggerFormat: Option[String] = None
  }

  implicit val floatFormParam = new FormParameterType[Float] {
    override def swaggerType: String = "number"

    override def swaggerFormat: Option[String] = Some("float")
  }

  implicit val doubleFormParam = new FormParameterType[Double] {
    override def swaggerType: String = "number"

    override def swaggerFormat: Option[String] = Some("double")
  }

  implicit val booleanFormParam = new FormParameterType[Boolean] {
    override def swaggerType: String = "boolean"

    override def swaggerFormat: Option[String] = None
  }

  implicit val integerFormParam = new FormParameterType[Int] {
    override def swaggerType: String = "integer"

    override def swaggerFormat: Option[String] = Some("int32")
  }

  implicit val longFormParam = new FormParameterType[Long] {
    override def swaggerType: String = "integer"

    override def swaggerFormat: Option[String] = Some("int64")
  }

  implicit def arrayFormParam[U: FormParameterType] = new FormParameterType[Seq[U]] {
    override def swaggerType: String = "array"

    override def swaggerFormat: Option[String] = None
  }

  implicit val fileFormParam = new FormParameterType[File] {
    override def swaggerType: String = "file"

    override def swaggerFormat: Option[String] = None
  }
}
