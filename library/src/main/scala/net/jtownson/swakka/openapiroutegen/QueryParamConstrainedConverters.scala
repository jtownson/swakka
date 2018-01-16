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
import akka.http.scaladsl.server.Directives.{parameter, _}
import akka.http.scaladsl.unmarshalling.PredefinedFromStringUnmarshallers._
import net.jtownson.swakka.coreroutegen.ConvertibleToDirective.instance
import net.jtownson.swakka.coreroutegen._
import net.jtownson.swakka.openapimodel._
import net.jtownson.swakka.openapiroutegen.ParamValidator._
import RouteGenTemplates._


trait QueryParamConstrainedConverters {

  type QueryParamConstrainedConverter[T, U] =
    ConvertibleToDirective.Aux[QueryParameterConstrained[T, U], T]

  implicit val stringReqQueryConverterConstrained
    : QueryParamConstrainedConverter[String, String] =
    instance((_: String, qp: QueryParameterConstrained[String, String]) => {
      constrainedQueryParameterTemplate(
        () => parameter(qp.name),
        (default: String) => parameter(qp.name.?(default)),
        stringValidator,
        qp
      )
    })

  implicit val floatReqQueryConverterConstrained
    : QueryParamConstrainedConverter[Float, Float] =
    instance((_: String, qp: QueryParameterConstrained[Float, Float]) => {
      constrainedQueryParameterTemplate(
        () => parameter(qp.name.as[Float]),
        (default: Float) => parameter(qp.name.as[Float].?(default)),
        numberValidator,
        qp
      )
    })

  implicit val doubleReqQueryConverterConstrained
    : QueryParamConstrainedConverter[Double, Double] =
    instance((_: String, qp: QueryParameterConstrained[Double, Double]) => {
      constrainedQueryParameterTemplate(
        () => parameter(qp.name.as[Double]),
        (default: Double) => parameter(qp.name.as[Double].?(default)),
        numberValidator,
        qp
      )
    })

  implicit val booleanReqQueryConverterConstrained
    : QueryParamConstrainedConverter[Boolean, Boolean] =
    instance((_: String, qp: QueryParameterConstrained[Boolean, Boolean]) => {
      constrainedQueryParameterTemplate(
        () => parameter(qp.name.as[Boolean]),
        (default: Boolean) => parameter(qp.name.as[Boolean].?(default)),
        anyValidator,
        qp
      )
    })

  implicit val intReqQueryConverterConstrained
    : QueryParamConstrainedConverter[Int, Int] =
    instance((_: String, qp: QueryParameterConstrained[Int, Int]) => {
      constrainedQueryParameterTemplate(
        () => parameter(qp.name.as[Int]),
        (default: Int) => parameter(qp.name.as[Int].?(default)),
        integralValidator,
        qp
      )
    })

  implicit val longReqQueryConverterConstrained
    : QueryParamConstrainedConverter[Long, Long] =
    instance((_: String, qp: QueryParameterConstrained[Long, Long]) => {
      constrainedQueryParameterTemplate(
        () => parameter(qp.name.as[Long]),
        (default: Long) => parameter(qp.name.as[Long].?(default)),
        integralValidator,
        qp
      )
    })

  implicit val stringOptQueryConverterConstrained
    : QueryParamConstrainedConverter[Option[String], String] = {
    instance(
      (_: String, qp: QueryParameterConstrained[Option[String], String]) => {
        constrainedQueryParameterTemplate(
          () => parameter(qp.name.?),
          (default: Option[String]) =>
            parameter(qp.name.?(default.get)).map(Option(_)),
          optionValidator(stringValidator),
          qp
        )
      })
  }

  implicit val floatOptQueryConverterConstrained
    : QueryParamConstrainedConverter[Option[Float], Float] =
    instance(
      (_: String, qp: QueryParameterConstrained[Option[Float], Float]) => {
        constrainedQueryParameterTemplate(
          () => parameter(qp.name.as[Float].?),
          (default: Option[Float]) =>
            parameter(qp.name.as[Float].?(default.get)).map(Option(_)),
          optionValidator(numberValidator),
          qp
        )
      })

  implicit val doubleOptQueryConverterConstrained
    : QueryParamConstrainedConverter[Option[Double], Double] =
    instance(
      (_: String, qp: QueryParameterConstrained[Option[Double], Double]) => {
        constrainedQueryParameterTemplate(
          () => parameter(qp.name.as[Double].?),
          (default: Option[Double]) =>
            parameter(qp.name.as[Double].?(default.get)).map(Option(_)),
          optionValidator(numberValidator),
          qp
        )
      })

  implicit val booleanOptQueryConverterConstrained
    : QueryParamConstrainedConverter[Option[Boolean], Boolean] =
    instance(
      (_: String, qp: QueryParameterConstrained[Option[Boolean], Boolean]) => {
        constrainedQueryParameterTemplate(
          () => parameter(qp.name.as[Boolean].?),
          (default: Option[Boolean]) =>
            parameter(qp.name.as[Boolean].?(default.get)).map(Option(_)),
          optionValidator(anyValidator),
          qp
        )
      })

  implicit val intOptQueryConverterConstrained
    : QueryParamConstrainedConverter[Option[Int], Int] =
    instance((_: String, qp: QueryParameterConstrained[Option[Int], Int]) => {
      constrainedQueryParameterTemplate(
        () => parameter(qp.name.as[Int].?),
        (default: Option[Int]) =>
          parameter(qp.name.as[Int].?(default.get)).map(Option(_)),
        optionValidator(integralValidator),
        qp
      )
    })

  implicit val longOptQueryConverterConstrained
    : QueryParamConstrainedConverter[Option[Long], Long] =
    instance((_: String, qp: QueryParameterConstrained[Option[Long], Long]) => {
      constrainedQueryParameterTemplate(
        () => parameter(qp.name.as[Long].?),
        (default: Option[Long]) =>
          parameter(qp.name.as[Long].?(default.get)).map(Option(_)),
        optionValidator(integralValidator),
        qp
      )
    })
}
