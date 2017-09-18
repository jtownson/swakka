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

package net.jtownson.swakka.routegen

import net.jtownson.swakka.model.Parameters.FormFieldParameter
import RouteGenTemplates._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.directives.FileInfo
import akka.stream.scaladsl.Source
import akka.util.ByteString

trait FormFieldParamConverters {

  implicit val stringReqFormFieldConverter: ConvertibleToDirective[FormFieldParameter[String]] =
    (_: String, fp: FormFieldParameter[String]) => {
      formFieldTemplate(
        () => formField(fp.name),
        (default: String) => formField(fp.name.?(default)),
        (value: String) => enumCase(fp, value),
        fp
      )
    }

  implicit val booleanReqFormFieldConverter: ConvertibleToDirective[FormFieldParameter[Boolean]] =
    (_: String, fp: FormFieldParameter[Boolean]) => {
      formFieldTemplate(
        () => formField(fp.name.as[Boolean]),
        (default: Boolean) => formField(fp.name.as[Boolean].?(default)),
        (value: Boolean) => enumCase(fp, value),
        fp
      )
    }

  implicit val intReqFormFieldConverter: ConvertibleToDirective[FormFieldParameter[Int]] =
    (_: String, fp: FormFieldParameter[Int]) => {
      formFieldTemplate(
        () => formField(fp.name.as[Int]),
        (default: Int) => formField(fp.name.as[Int].?(default)),
        (value: Int) => enumCase(fp, value),
        fp
      )
    }

  implicit val longReqFormFieldConverter: ConvertibleToDirective[FormFieldParameter[Long]] =
    (_: String, fp: FormFieldParameter[Long]) => {
      formFieldTemplate(
        () => formField(fp.name.as[Long]),
        (default: Long) => formField(fp.name.as[Long].?(default)),
        (value: Long) => enumCase(fp, value),
        fp
      )
    }

  implicit val floatReqFormFieldConverter: ConvertibleToDirective[FormFieldParameter[Float]] =
    (_: String, fp: FormFieldParameter[Float]) => {
      formFieldTemplate(
        () => formField(fp.name.as[Float]),
        (default: Float) => formField(fp.name.as[Float].?(default)),
        (value: Float) => enumCase(fp, value),
        fp
      )
    }

  implicit val doubleReqFormFieldConverter: ConvertibleToDirective[FormFieldParameter[Double]] =
    (_: String, fp: FormFieldParameter[Double]) => {
      formFieldTemplate(
        () => formField(fp.name.as[Double]),
        (default: Double) => formField(fp.name.as[Double].?(default)),
        (value: Double) => enumCase(fp, value),
        fp
      )
    }

  implicit val fileReqFormFieldConverter: ConvertibleToDirective[FormFieldParameter[(FileInfo, Source[ByteString, Any])]] =
    (_: String, fp: FormFieldParameter[(FileInfo, Source[ByteString, Any])]) => {
      fileUpload(fp.name.name).map(close(fp))
    }
}
