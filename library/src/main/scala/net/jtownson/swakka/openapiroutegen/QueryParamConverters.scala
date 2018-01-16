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

import akka.http.scaladsl.server.Directives.{parameter, _}
import akka.http.scaladsl.unmarshalling.PredefinedFromStringUnmarshallers._
import net.jtownson.swakka.openapimodel._
import net.jtownson.swakka.coreroutegen._
import RouteGenTemplates._
import net.jtownson.swakka.coreroutegen.ConvertibleToDirective.instance

trait QueryParamConverters {

  type QueryParamConverter[T] = ConvertibleToDirective.Aux[QueryParameter[T], T]

  implicit val stringReqQueryConverter: QueryParamConverter[String] =
    instance((_: String, qp: QueryParameter[String]) => {
      queryParameterTemplate(
        () => parameter(qp.name),
        (default: String) => parameter(qp.name.?(default)),
        qp
      )
    })

  implicit val stringOptQueryConverter: QueryParamConverter[Option[String]] = {
    instance((_: String, qp: QueryParameter[Option[String]]) => {
      queryParameterTemplate(
        () => parameter(qp.name.?),
        (default: Option[String]) => parameter(qp.name.?(default.get)).map(Option(_)),
        qp
      )
    })
  }

  implicit val floatReqQueryConverter: QueryParamConverter[Float] =
    instance((_: String, qp: QueryParameter[Float]) => {
      queryParameterTemplate(
        () => parameter(qp.name.as[Float]),
        (default: Float) => parameter(qp.name.as[Float].?(default)),
        qp
      )
    })

  implicit val floatOptQueryConverter: QueryParamConverter[Option[Float]] =
    instance((_: String, qp: QueryParameter[Option[Float]]) => {
      queryParameterTemplate(
        () => parameter(qp.name.as[Float].?),
        (default: Option[Float]) => parameter(qp.name.as[Float].?(default.get)).map(Option(_)),
        qp
      )
    })

  implicit val doubleReqQueryConverter: QueryParamConverter[Double] =
    instance((_: String, qp: QueryParameter[Double]) => {
      queryParameterTemplate(
        () => parameter(qp.name.as[Double]),
        (default: Double) => parameter(qp.name.as[Double].?(default)),
        qp
      )
    })

  implicit val doubleOptQueryConverter: QueryParamConverter[Option[Double]] =
    instance((_: String, qp: QueryParameter[Option[Double]]) => {
      queryParameterTemplate(
        () => parameter(qp.name.as[Double].?),
        (default: Option[Double]) => parameter(qp.name.as[Double].?(default.get)).map(Option(_)),
        qp
      )
    })

  implicit val booleanReqQueryConverter: QueryParamConverter[Boolean] =
    instance((_: String, qp: QueryParameter[Boolean]) => {
      queryParameterTemplate(
        () => parameter(qp.name.as[Boolean]),
        (default: Boolean) => parameter(qp.name.as[Boolean].?(default)),
        qp
      )
    })

  implicit val booleanOptQueryConverter: QueryParamConverter[Option[Boolean]] =
    instance((_: String, qp: QueryParameter[Option[Boolean]]) => {
      queryParameterTemplate(
        () => parameter(qp.name.as[Boolean].?),
        (default: Option[Boolean]) => parameter(qp.name.as[Boolean].?(default.get)).map(Option(_)),
        qp
      )
    })

  implicit val intReqQueryConverter: QueryParamConverter[Int] =
    instance((_: String, qp: QueryParameter[Int]) => {
      queryParameterTemplate(
        () => parameter(qp.name.as[Int]),
        (default: Int) => parameter(qp.name.as[Int].?(default)),
        qp
      )
    })

  implicit val intOptQueryConverter: QueryParamConverter[Option[Int]] =
    instance((_: String, qp: QueryParameter[Option[Int]]) => {
      queryParameterTemplate(
        () => parameter(qp.name.as[Int].?),
        (default: Option[Int]) => parameter(qp.name.as[Int].?(default.get)).map(Option(_)),
        qp
      )
    })

  implicit val longReqQueryConverter: QueryParamConverter[Long] =
    instance((_: String, qp: QueryParameter[Long]) => {
      queryParameterTemplate(
        () => parameter(qp.name.as[Long]),
        (default: Long) => parameter(qp.name.as[Long].?(default)),
        qp
      )
    })

  implicit val longOptQueryConverter: QueryParamConverter[Option[Long]] =
    instance((_: String, qp: QueryParameter[Option[Long]]) => {
      queryParameterTemplate(
        () => parameter(qp.name.as[Long].?),
        (default: Option[Long]) => parameter(qp.name.as[Long].?(default.get)).map(Option(_)),
        qp
      )
    })
}
