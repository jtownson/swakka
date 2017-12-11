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

trait QueryParamConverters {

  implicit val stringReqQueryConverter: ConvertibleToDirective[QueryParameter[String]] =
    (_: String, qp: QueryParameter[String]) => {
      parameterTemplate(
        () => parameter(qp.name),
        (default: String) => parameter(qp.name.?(default)),
        (value: String) => enumCase(qp, value),
        qp
      )
    }

  implicit val stringOptQueryConverter: ConvertibleToDirective[QueryParameter[Option[String]]] = {
    (_: String, qp: QueryParameter[Option[String]]) => {
      parameterTemplate(
        () => parameter(qp.name.?),
        (default: Option[String]) => parameter(qp.name.?(default.get)).map(Option(_)),
        (value: Option[String]) => enumCase(qp, value),
        qp
      )
    }
  }

  implicit val floatReqQueryConverter: ConvertibleToDirective[QueryParameter[Float]] =
    (_: String, qp: QueryParameter[Float]) => {
      parameterTemplate(
        () => parameter(qp.name.as[Float]),
        (default: Float) => parameter(qp.name.as[Float].?(default)),
        (value: Float) => enumCase(qp, value),
        qp
      )
    }

  implicit val floatOptQueryConverter: ConvertibleToDirective[QueryParameter[Option[Float]]] =
    (_: String, qp: QueryParameter[Option[Float]]) => {
      parameterTemplate(
        () => parameter(qp.name.as[Float].?),
        (default: Option[Float]) => parameter(qp.name.as[Float].?(default.get)).map(Option(_)),
        (value: Option[Float]) => enumCase(qp, value),
        qp
      )
    }

  implicit val doubleReqQueryConverter: ConvertibleToDirective[QueryParameter[Double]] =
    (_: String, qp: QueryParameter[Double]) => {
      parameterTemplate(
        () => parameter(qp.name.as[Double]),
        (default: Double) => parameter(qp.name.as[Double].?(default)),
        (value: Double) => enumCase(qp, value),
        qp
      )
    }

  implicit val doubleOptQueryConverter: ConvertibleToDirective[QueryParameter[Option[Double]]] =
    (_: String, qp: QueryParameter[Option[Double]]) => {
      parameterTemplate(
        () => parameter(qp.name.as[Double].?),
        (default: Option[Double]) => parameter(qp.name.as[Double].?(default.get)).map(Option(_)),
        (value: Option[Double]) => enumCase(qp, value),
        qp
      )
    }

  implicit val booleanReqQueryConverter: ConvertibleToDirective[QueryParameter[Boolean]] =
    (_: String, qp: QueryParameter[Boolean]) => {
      parameterTemplate(
        () => parameter(qp.name.as[Boolean]),
        (default: Boolean) => parameter(qp.name.as[Boolean].?(default)),
        (value: Boolean) => enumCase(qp, value),
        qp
      )
    }

  implicit val booleanOptQueryConverter: ConvertibleToDirective[QueryParameter[Option[Boolean]]] =
    (_: String, qp: QueryParameter[Option[Boolean]]) => {
      parameterTemplate(
        () => parameter(qp.name.as[Boolean].?),
        (default: Option[Boolean]) => parameter(qp.name.as[Boolean].?(default.get)).map(Option(_)),
        (value: Option[Boolean]) => enumCase(qp, value),
        qp
      )
    }

  implicit val intReqQueryConverter: ConvertibleToDirective[QueryParameter[Int]] =
    (_: String, qp: QueryParameter[Int]) => {
      parameterTemplate(
        () => parameter(qp.name.as[Int]),
        (default: Int) => parameter(qp.name.as[Int].?(default)),
        (value: Int) => enumCase(qp, value),
        qp
      )
    }

  implicit val intOptQueryConverter: ConvertibleToDirective[QueryParameter[Option[Int]]] =
    (_: String, qp: QueryParameter[Option[Int]]) => {
      parameterTemplate(
        () => parameter(qp.name.as[Int].?),
        (default: Option[Int]) => parameter(qp.name.as[Int].?(default.get)).map(Option(_)),
        (value: Option[Int]) => enumCase(qp, value),
        qp
      )
    }

  implicit val longReqQueryConverter: ConvertibleToDirective[QueryParameter[Long]] =
    (_: String, qp: QueryParameter[Long]) => {
      parameterTemplate(
        () => parameter(qp.name.as[Long]),
        (default: Long) => parameter(qp.name.as[Long].?(default)),
        (value: Long) => enumCase(qp, value),
        qp
      )
    }

  implicit val longOptQueryConverter: ConvertibleToDirective[QueryParameter[Option[Long]]] =
    (_: String, qp: QueryParameter[Option[Long]]) => {
      parameterTemplate(
        () => parameter(qp.name.as[Long].?),
        (default: Option[Long]) => parameter(qp.name.as[Long].?(default.get)).map(Option(_)),
        (value: Option[Long]) => enumCase(qp, value),
        qp
      )
    }
}
