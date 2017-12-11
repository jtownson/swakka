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

package net.jtownson.swakka.openapiroutegen

import net.jtownson.swakka.openapimodel._
import net.jtownson.swakka.coreroutegen._
import net.jtownson.swakka.coreroutegen.ConvertibleToDirective.instance
import RouteGenTemplates._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.directives.FileInfo
import akka.stream.scaladsl.Source
import akka.util.ByteString

trait FormFieldParamConverters {

  type FormFieldParamConverter[U] = ConvertibleToDirective.Aux[FormFieldParameter[U], U]

  implicit val stringReqFormFieldConverter: FormFieldParamConverter[String] =
    instance((_: String, fp: FormFieldParameter[String]) => {
      formFieldTemplate(
        () => formField(fp.name),
        (default: String) => formField(fp.name.?(default)),
        (value: String) => enumCase(fp, value),
        fp
      )
    })

  implicit val booleanReqFormFieldConverter: FormFieldParamConverter[Boolean] =
    instance((_: String, fp: FormFieldParameter[Boolean]) => {
      formFieldTemplate(
        () => formField(fp.name.as[Boolean]),
        (default: Boolean) => formField(fp.name.as[Boolean].?(default)),
        (value: Boolean) => enumCase(fp, value),
        fp
      )
    })

  implicit val intReqFormFieldConverter: FormFieldParamConverter[Int] =
    instance((_: String, fp: FormFieldParameter[Int]) => {
      formFieldTemplate(
        () => formField(fp.name.as[Int]),
        (default: Int) => formField(fp.name.as[Int].?(default)),
        (value: Int) => enumCase(fp, value),
        fp
      )
    })

  implicit val longReqFormFieldConverter: FormFieldParamConverter[Long] =
    instance((_: String, fp: FormFieldParameter[Long]) => {
      formFieldTemplate(
        () => formField(fp.name.as[Long]),
        (default: Long) => formField(fp.name.as[Long].?(default)),
        (value: Long) => enumCase(fp, value),
        fp
      )
    })

  implicit val floatReqFormFieldConverter: FormFieldParamConverter[Float] =
    instance((_: String, fp: FormFieldParameter[Float]) => {
      formFieldTemplate(
        () => formField(fp.name.as[Float]),
        (default: Float) => formField(fp.name.as[Float].?(default)),
        (value: Float) => enumCase(fp, value),
        fp
      )
    })

  implicit val doubleReqFormFieldConverter: FormFieldParamConverter[Double] =
    instance((_: String, fp: FormFieldParameter[Double]) => {
      formFieldTemplate(
        () => formField(fp.name.as[Double]),
        (default: Double) => formField(fp.name.as[Double].?(default)),
        (value: Double) => enumCase(fp, value),
        fp
      )
    })

  implicit val fileReqFormFieldConverter: FormFieldParamConverter[(FileInfo, Source[ByteString, Any])] =
    instance((_: String, fp: FormFieldParameter[(FileInfo, Source[ByteString, Any])]) => {
      formFieldTemplate(
        () => fileUpload(fp.name.name),
        (default: (FileInfo, Source[ByteString, Any])) => optionalFileUpload(fp.name.name).flatMap(optionalField => provide(optionalField.getOrElse(default))),
        (value: (FileInfo, Source[ByteString, Any])) => enumCase(fp, value),
        fp
      )
    })

  implicit val fileOptFormFieldConverter: FormFieldParamConverter[Option[(FileInfo, Source[ByteString, Any])]] =
    instance((_: String, fp: FormFieldParameter[Option[(FileInfo, Source[ByteString, Any])]]) =>
      formFieldTemplate(
        () => optionalFileUpload(fp.name.name),
        (default: Option[(FileInfo, Source[ByteString, Any])]) => optionalFileUpload(fp.name.name).map(optionalField => if (optionalField.isDefined) optionalField else default),
        (value: Option[(FileInfo, Source[ByteString, Any])]) => enumCase(fp, value),
        fp
      )
    )

  implicit val stringOptFormFieldConverter: FormFieldParamConverter[Option[String]] =
    instance((_: String, fp: FormFieldParameter[Option[String]]) => {
      formFieldTemplate(
        () => formField(fp.name.?),
        (default: Option[String]) => formField(fp.name.?(default.get)).map(Option(_)),
        (value: Option[String]) => enumCase(fp, value),
        fp
      )
    })

  implicit val booleanOptFormFieldConverter: FormFieldParamConverter[Option[Boolean]] =
    instance((_: String, fp: FormFieldParameter[Option[Boolean]]) => {
      formFieldTemplate(
        () => formField(fp.name.as[Boolean].?),
        (default: Option[Boolean]) => formField(fp.name.as[Boolean].?(default.get)).map(Option(_)),
        (value: Option[Boolean]) => enumCase(fp, value),
        fp
      )
    })

  implicit val intOptFormFieldConverter: FormFieldParamConverter[Option[Int]] =
    instance((_: String, fp: FormFieldParameter[Option[Int]]) => {
      formFieldTemplate(
        () => formField(fp.name.as[Int].?),
        (default: Option[Int])=> formField(fp.name.as[Int].?(default.get)).map(Option(_)),
        (value: Option[Int]) => enumCase(fp, value),
        fp
      )
    })

  implicit val longOptFormFieldConverter: FormFieldParamConverter[Option[Long]] =
    instance((_: String, fp: FormFieldParameter[Option[Long]]) => {
      formFieldTemplate(
        () => formField(fp.name.as[Long].?),
        (default: Option[Long]) => formField(fp.name.as[Long].?(default.get)).map(Option(_)),
        (value: Option[Long]) => enumCase(fp, value),
        fp
      )
    })

  implicit val floatOptFormFieldConverter: FormFieldParamConverter[Option[Float]] =
    instance((_: String, fp: FormFieldParameter[Option[Float]]) => {
      formFieldTemplate(
        () => formField(fp.name.as[Float].?),
        (default: Option[Float]) => formField(fp.name.as[Float].?(default.get)).map(Option(_)),
        (value: Option[Float]) => enumCase(fp, value),
        fp
      )
    })

  implicit val doubleOptFormFieldConverter: FormFieldParamConverter[Option[Double]] =
    instance((_: String, fp: FormFieldParameter[Option[Double]]) => {
      formFieldTemplate(
        () => formField(fp.name.as[Double].?),
        (default: Option[Double]) => formField(fp.name.as[Double].?(default.get)).map(Option(_)),
        (value: Option[Double]) => enumCase(fp, value),
        fp
      )
    })
}
