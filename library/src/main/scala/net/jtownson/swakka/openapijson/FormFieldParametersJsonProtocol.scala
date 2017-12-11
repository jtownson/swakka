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

import spray.json.{JsBoolean, JsString, JsValue}

import net.jtownson.swakka.misc.jsObject
import net.jtownson.swakka.openapimodel._

trait FormFieldParametersJsonProtocol {

  implicit val requiredStringFormDataParameterFormat: ParameterJsonFormat[FormFieldParameter[String]] =
    (fdp: FormFieldParameter[String]) =>
      formDataItem(fdp.name.name, fdp.description, true, "string", None)

  implicit val requiredBooleanFormDataParameterFormat: ParameterJsonFormat[FormFieldParameter[Boolean]] =
    (fdp: FormFieldParameter[Boolean]) =>
      formDataItem(fdp.name.name, fdp.description, true, "boolean", None)

  implicit val requiredIntFormDataParameterFormat: ParameterJsonFormat[FormFieldParameter[Int]] =
    (fdp: FormFieldParameter[Int]) =>
      formDataItem(fdp.name.name, fdp.description, true, "integer", Some("int32"))

  implicit val requiredLongFormDataParameterFormat: ParameterJsonFormat[FormFieldParameter[Long]] =
    (fdp: FormFieldParameter[Long]) =>
      formDataItem(fdp.name.name, fdp.description, true, "integer", Some("int64"))

  implicit val requiredFloatFormDataParameterFormat: ParameterJsonFormat[FormFieldParameter[Float]] =
    (fdp: FormFieldParameter[Float]) =>
      formDataItem(fdp.name.name, fdp.description, true, "number", Some("float"))

  implicit val requiredDoubleFormDataParameterFormat: ParameterJsonFormat[FormFieldParameter[Double]] =
    (fdp: FormFieldParameter[Double]) =>
      formDataItem(fdp.name.name, fdp.description, true, "number", Some("double"))

  implicit val requiredFileFormDataParameterFormat: ParameterJsonFormat[FormFieldParameter[(FileInfo, Source[ByteString, Any])]] =
    (fdp: FormFieldParameter[(FileInfo, Source[ByteString, Any])]) =>
      formDataItem(fdp.name.name, fdp.description, true, "file", None)


  implicit val optionalStringFormDataParameterFormat: ParameterJsonFormat[FormFieldParameter[Option[String]]] =
    (fdp: FormFieldParameter[Option[String]]) =>
      formDataItem(fdp.name.name, fdp.description, false, "string", None)

  implicit val optionalBooleanFormDataParameterFormat: ParameterJsonFormat[FormFieldParameter[Option[Boolean]]] =
    (fdp: FormFieldParameter[Option[Boolean]]) =>
      formDataItem(fdp.name.name, fdp.description, false, "boolean", None)

  implicit val optionalIntFormDataParameterFormat: ParameterJsonFormat[FormFieldParameter[Option[Int]]] =
    (fdp: FormFieldParameter[Option[Int]]) =>
      formDataItem(fdp.name.name, fdp.description, false, "integer", Some("int32"))

  implicit val optionalLongFormDataParameterFormat: ParameterJsonFormat[FormFieldParameter[Option[Long]]] =
    (fdp: FormFieldParameter[Option[Long]]) =>
      formDataItem(fdp.name.name, fdp.description, false, "integer", Some("int64"))

  implicit val optionalFloatFormDataParameterFormat: ParameterJsonFormat[FormFieldParameter[Option[Float]]] =
    (fdp: FormFieldParameter[Option[Float]]) =>
      formDataItem(fdp.name.name, fdp.description, false, "number", Some("float"))

  implicit val optionalDoubleFormDataParameterFormat: ParameterJsonFormat[FormFieldParameter[Option[Double]]] =
    (fdp: FormFieldParameter[Option[Double]]) =>
      formDataItem(fdp.name.name, fdp.description, false, "number", Some("double"))

  implicit val optionalFileFormDataParameterFormat: ParameterJsonFormat[FormFieldParameter[Option[(FileInfo, Source[ByteString, Any])]]] =
    (fdp: FormFieldParameter[Option[(FileInfo, Source[ByteString, Any])]]) =>
      formDataItem(fdp.name.name, fdp.description, false, "file", None)

  private def formDataItem(name: String, description: Option[String], required: Boolean, `type`: String, format: Option[String]): JsValue = {
    jsObject(
      Some("name" -> JsString(name)),
      Some("type" -> JsString(`type`)),
      format.map("format" -> JsString(_)),
      Some("in" -> JsString("formData")),
      description.map("description" -> JsString(_)),
      Some("required" -> JsBoolean(required))
    )
  }
}

object FormFieldParametersJsonProtocol extends FormFieldParametersJsonProtocol
