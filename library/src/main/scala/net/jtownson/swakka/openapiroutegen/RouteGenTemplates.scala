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
import akka.http.scaladsl.server.Directives.{provide, reject}
import net.jtownson.swakka.openapimodel._
object RouteGenTemplates {

  def headerTemplate[T](fNoDefault: () => Directive1[T],
                                fDefault: T => Directive1[T],
                                hp: HeaderParameter[T]): Directive1[T] =
    hp.default.fold(fNoDefault())(fDefault(_))

  def queryParameterTemplate[T](fNoDefault: () => Directive1[T],
                                   fDefault: T => Directive1[T],
                                   qp: QueryParameter[T]): Directive1[T] =
    qp.default.fold(fNoDefault())(fDefault(_))

  def formFieldTemplate[T](fNoDefault: () => Directive1[T],
                           fDefault: T => Directive1[T],
                           fp: FormFieldParameter[T]): Directive1[T] =
    fp.default.fold(fNoDefault())(fDefault(_))

  def constrainedQueryParameterTemplate[T, U](
                               fNoDefault: () => Directive1[T],
                               fDefault: T => Directive1[T],
                               validator: ParamValidator[T, U],
                               qp: QueryParameterConstrained[T, U]): Directive1[T] = {

    val fValidate: T => Directive1[T] =
      t =>
        validator
          .validate(qp.constraints, t)
          .fold(errors => rejectWithValidationErrors(errors),
            value => provide(value))

    qp.default match {
      case Some(default) =>
        fDefault(default).flatMap(fValidate)
      case None =>
        fNoDefault().flatMap(fValidate)
    }
  }

  private def rejectWithValidationErrors[T](validationErrors: String): Directive1[T] =
    reject(ValidationRejection(validationErrors))

}
