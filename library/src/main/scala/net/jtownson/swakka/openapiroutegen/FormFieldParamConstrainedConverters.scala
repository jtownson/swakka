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

import akka.http.scaladsl.server.{Directive1, ValidationRejection}
import akka.http.scaladsl.server.Directives._
import net.jtownson.swakka.coreroutegen.ConvertibleToDirective.instance
import net.jtownson.swakka.coreroutegen._
import net.jtownson.swakka.openapimodel._
import net.jtownson.swakka.openapiroutegen.ParamValidator._

trait FormFieldParamConstrainedConverters {

  type FormFieldParamConstrainedConverter[T, U] =
    ConvertibleToDirective.Aux[FormFieldParameterConstrained[T, U], T]

  implicit val stringReqFormFieldConverterConstrained
    : FormFieldParamConstrainedConverter[String, String] =
    instance((_: String, fp: FormFieldParameterConstrained[String, String]) => {
      formFieldTemplate(
        () => formField(fp.name),
        (default: String) => formField(fp.name.?(default)),
        stringValidator,
        fp
      )
    })

  implicit val booleanReqFormFieldConverterConstrained
    : FormFieldParamConstrainedConverter[Boolean, Boolean] =
    instance(
      (_: String, fp: FormFieldParameterConstrained[Boolean, Boolean]) => {
        formFieldTemplate(
          () => formField(fp.name.as[Boolean]),
          (default: Boolean) => formField(fp.name.as[Boolean].?(default)),
          anyValidator,
          fp
        )
      })

  implicit val intReqFormFieldConverterConstrained
    : FormFieldParamConstrainedConverter[Int, Int] =
    instance((_: String, fp: FormFieldParameterConstrained[Int, Int]) => {
      formFieldTemplate(
        () => formField(fp.name.as[Int]),
        (default: Int) => formField(fp.name.as[Int].?(default)),
        integralValidator,
        fp
      )
    })

  implicit val longReqFormFieldConverterConstrained
    : FormFieldParamConstrainedConverter[Long, Long] =
    instance((_: String, fp: FormFieldParameterConstrained[Long, Long]) => {
      formFieldTemplate(
        () => formField(fp.name.as[Long]),
        (default: Long) => formField(fp.name.as[Long].?(default)),
        integralValidator,
        fp
      )
    })

  implicit val floatReqFormFieldConverterConstrained
    : FormFieldParamConstrainedConverter[Float, Float] =
    instance((_: String, fp: FormFieldParameterConstrained[Float, Float]) => {
      formFieldTemplate(
        () => formField(fp.name.as[Float]),
        (default: Float) => formField(fp.name.as[Float].?(default)),
        numberValidator,
        fp
      )
    })

  implicit val doubleReqFormFieldConverterConstrained
    : FormFieldParamConstrainedConverter[Double, Double] =
    instance((_: String, fp: FormFieldParameterConstrained[Double, Double]) => {
      formFieldTemplate(
        () => formField(fp.name.as[Double]),
        (default: Double) => formField(fp.name.as[Double].?(default)),
        numberValidator,
        fp
      )
    })

  implicit val stringOptFormFieldConverterConstrained
    : FormFieldParamConstrainedConverter[Option[String], String] =
    instance(
      (_: String,
       fp: FormFieldParameterConstrained[Option[String], String]) => {
        formFieldTemplate(
          () => formField(fp.name.?),
          (default: Option[String]) =>
            formField(fp.name.?(default.get)).map(Option(_)),
          optionValidator(stringValidator),
          fp
        )
      })

  implicit val booleanOptFormFieldConverterConstrained
    : FormFieldParamConstrainedConverter[Option[Boolean], Boolean] =
    instance(
      (_: String,
       fp: FormFieldParameterConstrained[Option[Boolean], Boolean]) => {
        formFieldTemplate(
          () => formField(fp.name.as[Boolean].?),
          (default: Option[Boolean]) =>
            formField(fp.name.as[Boolean].?(default.get)).map(Option(_)),
          optionValidator(anyValidator),
          fp
        )
      })

  implicit val intOptFormFieldConverterConstrained
    : FormFieldParamConstrainedConverter[Option[Int], Int] =
    instance(
      (_: String, fp: FormFieldParameterConstrained[Option[Int], Int]) => {
        formFieldTemplate(
          () => formField(fp.name.as[Int].?),
          (default: Option[Int]) =>
            formField(fp.name.as[Int].?(default.get)).map(Option(_)),
          optionValidator(integralValidator),
          fp
        )
      })

  implicit val longOptFormFieldConverterConstrained
    : FormFieldParamConstrainedConverter[Option[Long], Long] =
    instance(
      (_: String, fp: FormFieldParameterConstrained[Option[Long], Long]) => {
        formFieldTemplate(
          () => formField(fp.name.as[Long].?),
          (default: Option[Long]) =>
            formField(fp.name.as[Long].?(default.get)).map(Option(_)),
          optionValidator(integralValidator),
          fp
        )
      })

  implicit val floatOptFormFieldConverterConstrained
    : FormFieldParamConstrainedConverter[Option[Float], Float] =
    instance(
      (_: String, fp: FormFieldParameterConstrained[Option[Float], Float]) => {
        formFieldTemplate(
          () => formField(fp.name.as[Float].?),
          (default: Option[Float]) =>
            formField(fp.name.as[Float].?(default.get)).map(Option(_)),
          optionValidator(numberValidator),
          fp
        )
      })

  implicit val doubleOptFormFieldConverterConstrained
    : FormFieldParamConstrainedConverter[Option[Double], Double] =
    instance(
      (_: String,
       fp: FormFieldParameterConstrained[Option[Double], Double]) => {
        formFieldTemplate(
          () => formField(fp.name.as[Double].?),
          (default: Option[Double]) =>
            formField(fp.name.as[Double].?(default.get)).map(Option(_)),
          optionValidator(numberValidator),
          fp
        )
      })

  private def formFieldTemplate[T, U](
      fNoDefault: () => Directive1[T],
      fDefault: T => Directive1[T],
      validator: ParamValidator[T, U],
      fp: FormFieldParameterConstrained[T, U]): Directive1[T] = {

    val fValidate: T => Directive1[T] =
      t =>
        validator
          .validate(fp.constraints, t)
          .fold(errors => rejectWithValidationErrors(errors),
                value => provide(value))

    fp.default match {
      case Some(default) =>
        fDefault(default).flatMap(fValidate)
      case None =>
        fNoDefault().flatMap(fValidate)
    }
  }

  private def rejectWithValidationErrors[T](
      validationErrors: String): Directive1[T] =
    reject(ValidationRejection(validationErrors))

}
