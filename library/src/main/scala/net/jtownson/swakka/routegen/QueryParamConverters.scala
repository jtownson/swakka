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

import akka.http.scaladsl.server.Directives.{parameter, _}
import akka.http.scaladsl.server.{Directive1, MissingQueryParamRejection, Rejection}
import akka.http.scaladsl.unmarshalling.PredefinedFromStringUnmarshallers._
import net.jtownson.swakka.model.Parameters.QueryParameter
import net.jtownson.swakka.model.Parameters.QueryParameter.OpenQueryParameter

trait QueryParamConverters {


  implicit val stringReqQueryConverter: ConvertibleToDirective[QueryParameter[String]] =
    (_: String, qp: QueryParameter[String]) => {
      parameterTemplate(
        () => parameter(qp.name),
        (default: String) => parameter(qp.name.?(default)),
        (value: String) => enumCase(MissingQueryParamRejection(qp.name.name), qp, value),
        qp
      )
    }

  implicit val stringOptQueryConverter: ConvertibleToDirective[QueryParameter[Option[String]]] =
    (_: String, qp: QueryParameter[Option[String]]) => {
      qp.default match {
        case Some(Some(default)) => parameter(qp.name.?(default)).map(os => close(qp)(Some(os)))
        case _ => parameter(qp.name.?).map(close(qp))
      }
    }

  implicit val floatReqQueryConverter: ConvertibleToDirective[QueryParameter[Float]] =
    (_: String, qp: QueryParameter[Float]) => {
      qp.default match {
        case Some(default) => parameter(qp.name.as[Float].?(default)).map(close(qp))
        case None => parameter(qp.name.as[Float]).map(close(qp))
      }
    }

  implicit val floatOptQueryConverter: ConvertibleToDirective[QueryParameter[Option[Float]]] =
    (_: String, qp: QueryParameter[Option[Float]]) => {
      qp.default match {
        case Some(Some(default)) => parameter(qp.name.as[Float].?(default)).map(of => close(qp)(Some(of)))
        case _ => parameter(qp.name.as[Float].?).map(close(qp))
      }
    }

  implicit val doubleReqQueryConverter: ConvertibleToDirective[QueryParameter[Double]] =
    (_: String, qp: QueryParameter[Double]) => {
      qp.default match {
        case Some(default) => parameter(qp.name.as[Double].?(default)).map(close(qp))
        case None => parameter(qp.name.as[Double]).map(close(qp))
      }
    }

  implicit val doubleOptQueryConverter: ConvertibleToDirective[QueryParameter[Option[Double]]] =
    (_: String, qp: QueryParameter[Option[Double]]) => {
      qp.default match {
        case Some(Some(default)) => parameter(qp.name.as[Double].?(default)).map(dp => close(qp)(Some(dp)))
        case _ => parameter(qp.name.as[Double].?).map(close(qp))
      }
    }

  implicit val booleanReqQueryConverter: ConvertibleToDirective[QueryParameter[Boolean]] =
    (_: String, qp: QueryParameter[Boolean]) => {
      qp.default match {
        case Some(default) => parameter(qp.name.as[Boolean].?(default)).map(close(qp))
        case None => parameter(qp.name.as[Boolean]).map(close(qp))
      }
    }

  implicit val booleanOptQueryConverter: ConvertibleToDirective[QueryParameter[Option[Boolean]]] =
    (_: String, qp: QueryParameter[Option[Boolean]]) => {
      qp.default match {
        case Some(Some(default)) => parameter(qp.name.as[Boolean].?(default)).map(bp => close(qp)(Some(bp)))
        case _ => parameter(qp.name.as[Boolean].?).map(close(qp))
      }
    }

  implicit val intReqQueryConverter: ConvertibleToDirective[QueryParameter[Int]] =
    (_: String, qp: QueryParameter[Int]) => {
      qp.default match {
        case Some(default) => parameter(qp.name.as[Int].?(default)).map(close(qp))
        case None => parameter(qp.name.as[Int]).map(close(qp))
      }
    }

  implicit val intOptQueryConverter: ConvertibleToDirective[QueryParameter[Option[Int]]] =
    (_: String, qp: QueryParameter[Option[Int]]) => {
      qp.default match {
        case Some(Some(default)) => parameter(qp.name.as[Int].?(default)).map(ip => close(qp)(Some(ip)))
        case _ => parameter(qp.name.as[Int].?).map(close(qp))
      }
    }

  implicit val longReqQueryConverter: ConvertibleToDirective[QueryParameter[Long]] =
    (_: String, qp: QueryParameter[Long]) => {
      qp.default match {
        case Some(default) => parameter(qp.name.as[Long].?(default)).map(close(qp))
        case None => parameter(qp.name.as[Long]).map(close(qp))
      }
    }

  implicit val longOptQueryConverter: ConvertibleToDirective[QueryParameter[Option[Long]]] =
    (_: String, qp: QueryParameter[Option[Long]]) => {
      qp.default match {
        case Some(Some(default)) => parameter(qp.name.as[Long].?(default)).map(lp => close(qp)(Some(lp)))
        case _ => parameter(qp.name.as[Long].?).map(close(qp))
      }
    }

  private def parameterTemplate[T](fNoDefault: () => Directive1[T],
                                   fDefault: T => Directive1[T],
                                   fEnum: T => Directive1[T],
                                   qp: QueryParameter[T]): Directive1[QueryParameter[T]] = {

    val extraction: Directive1[T] = qp.default match {
      case Some(default) =>
        fDefault(default).flatMap(fEnum)
      case None =>
        fNoDefault().flatMap(fEnum)
    }

    extraction.map(close(qp))
  }

  private def close[T](qp: QueryParameter[T]): T => QueryParameter[T] =
    t => qp.asInstanceOf[OpenQueryParameter[T]].closeWith(t)

  private def enumCase[T](rejection: Rejection, qp: QueryParameter[T], value: T): Directive1[T] = {
    qp.enum match {
      case None => provide(value)
      case Some(seq) if seq.contains(value) => provide(value)
      case _ => reject(rejection)
    }
  }
}
