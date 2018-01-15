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

package net.jtownson.swakka.openapijson

import akka.http.scaladsl.server.directives.FileInfo
import akka.stream.scaladsl.Source
import akka.util.ByteString

import net.jtownson.swakka.openapimodel._
import ParameterTemplates._

trait FormFieldParametersJsonProtocol {

  implicit val requiredStringFormDataParameterFormat: ParameterJsonFormat[FormFieldParameter[String]] =
    (fdp: FormFieldParameter[String]) =>
      simpleParam(in = "formData", name = fdp.name, description = fdp.description, required = true, `type` = "string", format = None)

  implicit val requiredBooleanFormDataParameterFormat: ParameterJsonFormat[FormFieldParameter[Boolean]] =
    (fdp: FormFieldParameter[Boolean]) =>
      simpleParam(in = "formData", name = fdp.name, description = fdp.description, required = true, `type` = "boolean", format = None)

  implicit val requiredIntFormDataParameterFormat: ParameterJsonFormat[FormFieldParameter[Int]] =
    (fdp: FormFieldParameter[Int]) =>
      simpleParam(in = "formData", name = fdp.name, description = fdp.description, required = true, `type` = "integer", format = Some("int32"))

  implicit val requiredLongFormDataParameterFormat: ParameterJsonFormat[FormFieldParameter[Long]] =
    (fdp: FormFieldParameter[Long]) =>
      simpleParam(in = "formData", name = fdp.name, description = fdp.description, required = true, `type` = "integer", format = Some("int64"))

  implicit val requiredFloatFormDataParameterFormat: ParameterJsonFormat[FormFieldParameter[Float]] =
    (fdp: FormFieldParameter[Float]) =>
      simpleParam(in = "formData", name = fdp.name, description = fdp.description, required = true, `type` = "number", format = Some("float"))

  implicit val requiredDoubleFormDataParameterFormat: ParameterJsonFormat[FormFieldParameter[Double]] =
    (fdp: FormFieldParameter[Double]) =>
      simpleParam(in = "formData", name = fdp.name, description = fdp.description, required = true, `type` = "number", format = Some("double"))

  implicit val requiredFileFormDataParameterFormat: ParameterJsonFormat[FormFieldParameter[(FileInfo, Source[ByteString, Any])]] =
    (fdp: FormFieldParameter[(FileInfo, Source[ByteString, Any])]) =>
      simpleParam(in = "formData", name = fdp.name, description = fdp.description, required = true, `type` = "file", format = None)


  implicit val optionalStringFormDataParameterFormat: ParameterJsonFormat[FormFieldParameter[Option[String]]] =
    (fdp: FormFieldParameter[Option[String]]) =>
      simpleParam(in = "formData", name = fdp.name, description = fdp.description, required = false, `type` = "string", format = None, default = defaultOf(fdp))

  implicit val optionalBooleanFormDataParameterFormat: ParameterJsonFormat[FormFieldParameter[Option[Boolean]]] =
    (fdp: FormFieldParameter[Option[Boolean]]) =>
      simpleParam(in = "formData", name = fdp.name, description = fdp.description, required = false, `type` = "boolean", format = None, default = defaultOf(fdp))

  implicit val optionalIntFormDataParameterFormat: ParameterJsonFormat[FormFieldParameter[Option[Int]]] =
    (fdp: FormFieldParameter[Option[Int]]) =>
      simpleParam(in = "formData", name = fdp.name, description = fdp.description, required = false, `type` = "integer", format = Some("int32"), default = defaultOf(fdp))

  implicit val optionalLongFormDataParameterFormat: ParameterJsonFormat[FormFieldParameter[Option[Long]]] =
    (fdp: FormFieldParameter[Option[Long]]) =>
      simpleParam(in = "formData", name = fdp.name, description = fdp.description, required = false, `type` = "integer", format = Some("int64"), default = defaultOf(fdp))

  implicit val optionalFloatFormDataParameterFormat: ParameterJsonFormat[FormFieldParameter[Option[Float]]] =
    (fdp: FormFieldParameter[Option[Float]]) =>
      simpleParam(in = "formData", name = fdp.name, description = fdp.description, required = false, `type` = "number", format = Some("float"), default = defaultOf(fdp))

  implicit val optionalDoubleFormDataParameterFormat: ParameterJsonFormat[FormFieldParameter[Option[Double]]] =
    (fdp: FormFieldParameter[Option[Double]]) =>
      simpleParam(in = "formData", name = fdp.name, description = fdp.description, required = false, `type` = "number", format = Some("double"), default = defaultOf(fdp))

  implicit val optionalFileFormDataParameterFormat: ParameterJsonFormat[FormFieldParameter[Option[(FileInfo, Source[ByteString, Any])]]] =
    (fdp: FormFieldParameter[Option[(FileInfo, Source[ByteString, Any])]]) =>
      simpleParam(in = "formData", name = fdp.name, description = fdp.description, required = false, `type` = "file", format = None, default = None)

}

object FormFieldParametersJsonProtocol extends FormFieldParametersJsonProtocol
