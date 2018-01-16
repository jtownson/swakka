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

import akka.http.scaladsl.server.Directives.{headerValueByName, optionalHeaderValueByName, provide, reject}
import akka.http.scaladsl.server.{Directive1, MissingHeaderRejection, ValidationRejection}
import net.jtownson.swakka.coreroutegen.ConvertibleToDirective.instance
import net.jtownson.swakka.coreroutegen._
import net.jtownson.swakka.openapimodel._
import net.jtownson.swakka.openapiroutegen.RouteGenTemplates._
import ParamValidator._

trait HeaderParamConstrainedConverters {

  type HeaderParamConstrainedConverter[T, U] = ConvertibleToDirective.Aux[HeaderParameterConstrained[T, U], T]

  implicit val stringReqHeaderConverterConstrained: HeaderParamConstrainedConverter[String, String] =
    requiredHeaderParamDirective(s => s, stringValidator)

  implicit val floatReqHeaderConverterConstrained: HeaderParamConstrainedConverter[Float, Float] =
    requiredHeaderParamDirective(_.toFloat, numberValidator)

  implicit val doubleReqHeaderConverterConstrained: HeaderParamConstrainedConverter[Double, Double] =
    requiredHeaderParamDirective(_.toDouble, numberValidator)

  implicit val booleanReqHeaderConverterConstrained: HeaderParamConstrainedConverter[Boolean, Boolean] =
    requiredHeaderParamDirective(_.toBoolean, anyValidator)

  implicit val intReqHeaderConverterConstrained: HeaderParamConstrainedConverter[Int, Int] =
    requiredHeaderParamDirective(_.toInt, integralValidator)

  implicit val longReqHeaderConverterConstrained: HeaderParamConstrainedConverter[Long, Long] =
    requiredHeaderParamDirective(_.toLong, integralValidator)


  implicit val stringOptHeaderConverterConstrained: HeaderParamConstrainedConverter[Option[String], String] =
    optionalHeaderParamDirective(s => s, stringValidator)

  implicit val floatOptHeaderConverterConstrained: HeaderParamConstrainedConverter[Option[Float], Float] =
    optionalHeaderParamDirective(_.toFloat, numberValidator)

  implicit val doubleOptHeaderConverterConstrained: HeaderParamConstrainedConverter[Option[Double], Double] =
    optionalHeaderParamDirective(_.toDouble, numberValidator)

  implicit val booleanOptHeaderConverterConstrained: HeaderParamConstrainedConverter[Option[Boolean], Boolean] =
    optionalHeaderParamDirective(_.toBoolean, anyValidator)

  implicit val intOptHeaderConverterConstrained: HeaderParamConstrainedConverter[Option[Int], Int] =
    optionalHeaderParamDirective(_.toInt, integralValidator)

  implicit val longOptHeaderConverterConstrained: HeaderParamConstrainedConverter[Option[Long], Long] =
    optionalHeaderParamDirective(_.toLong, integralValidator)

  private def requiredHeaderParamDirective[T](valueParser: String => T, validator: ParamValidator[T, T]):
  HeaderParamConstrainedConverter[T, T] =
    instance((_: String, hp: HeaderParameterConstrained[T, T]) => {
    headerTemplate(
      () => headerValueByName(hp.name).map(valueParser(_)),
      (default: T) => optionalHeaderValueByName(hp.name).map(extractIfPresent(valueParser, default)),
      validator,
      hp
    )
  })

  private def optionalHeaderParamDirective[T](valueParser: String => T, validator: ParamValidator[T, T]):
  HeaderParamConstrainedConverter[Option[T], T] =
    instance((_: String, hp: HeaderParameterConstrained[Option[T], T]) => {

    headerTemplate(
      () => optionalHeaderValueByName(hp.name).map(os => os.map(valueParser(_))),
      (default: Option[T]) => optionalHeaderValueByName(hp.name.name).map(extractIfPresent(valueParser, default)),
      optionValidator(validator),
      hp)
  })

  private def extractIfPresent[T](valueParser: String => T, default: T)(maybeHeader: Option[String]): T =
    maybeHeader match {
      case Some(header) => valueParser(header)
      case None => default
    }

  private def extractIfPresent[T](valueParser: String => T, default: Option[T])(maybeHeader: Option[String]): Option[T] =
    maybeHeader match {
      case Some(header) => Some(valueParser(header))
      case None => default
    }

  private def headerTemplate[T, U](fNoDefault: () => Directive1[T],
                        fDefault: T => Directive1[T],
                        validator: ParamValidator[T, U],
                        hp: HeaderParameterConstrained[T, U]): Directive1[T] = {

    val fValidate: T => Directive1[T] =
      t => validator.validate(hp.constraints, t).
        fold(errors => rejectWithValidationErrors(errors), value => provide(value))

    hp.default match {
      case Some(default) =>
        fDefault(default).flatMap(fValidate)
      case None =>
        fNoDefault().flatMap(fValidate)
    }
  }

  private def rejectWithValidationErrors[T](validationErrors: String): Directive1[T] =
    reject(ValidationRejection(validationErrors))
}
