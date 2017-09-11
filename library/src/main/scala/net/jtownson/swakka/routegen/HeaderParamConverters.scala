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

import akka.http.scaladsl.server.Directives.{headerValueByName, optionalHeaderValueByName}
import akka.http.scaladsl.server.MissingHeaderRejection
import net.jtownson.swakka.model.Parameters.HeaderParameter
import net.jtownson.swakka.routegen.RouteGenTemplates._

trait HeaderParamConverters {

  implicit val stringReqHeaderConverter: ConvertibleToDirective[HeaderParameter[String]] =
    requiredHeaderParamDirective(s => s)

  implicit val stringOptHeaderConverter: ConvertibleToDirective[HeaderParameter[Option[String]]] =
    optionalHeaderParamDirective(s => s)

  implicit val floatReqHeaderConverter: ConvertibleToDirective[HeaderParameter[Float]] =
    requiredHeaderParamDirective(_.toFloat)

  implicit val floatOptHeaderConverter: ConvertibleToDirective[HeaderParameter[Option[Float]]] =
    optionalHeaderParamDirective(_.toFloat)

  implicit val doubleReqHeaderConverter: ConvertibleToDirective[HeaderParameter[Double]] =
    requiredHeaderParamDirective(_.toDouble)

  implicit val doubleOptHeaderConverter: ConvertibleToDirective[HeaderParameter[Option[Double]]] =
    optionalHeaderParamDirective(_.toDouble)

  implicit val booleanReqHeaderConverter: ConvertibleToDirective[HeaderParameter[Boolean]] =
    requiredHeaderParamDirective(_.toBoolean)

  implicit val booleanOptHeaderConverter: ConvertibleToDirective[HeaderParameter[Option[Boolean]]] =
    optionalHeaderParamDirective(_.toBoolean)

  implicit val intReqHeaderConverter: ConvertibleToDirective[HeaderParameter[Int]] =
    requiredHeaderParamDirective(_.toInt)

  implicit val intOptHeaderConverter: ConvertibleToDirective[HeaderParameter[Option[Int]]] =
    optionalHeaderParamDirective(_.toInt)

  implicit val longReqHeaderConverter: ConvertibleToDirective[HeaderParameter[Long]] =
    requiredHeaderParamDirective(_.toLong)

  implicit val longOptHeaderConverter: ConvertibleToDirective[HeaderParameter[Option[Long]]] =
    optionalHeaderParamDirective(_.toLong)

  private def requiredHeaderParamDirective[T](valueParser: String => T):
  ConvertibleToDirective[HeaderParameter[T]] = (_: String, hp: HeaderParameter[T]) => {
    headerTemplate(
      () => headerValueByName(hp.name).map(valueParser(_)),
      (default: T) => optionalHeaderValueByName(hp.name).map(extractIfPresent(valueParser, default)),
      (value: T) => enumCase(MissingHeaderRejection(hp.name.name), hp, value),
      hp
    )
  }

  private def optionalHeaderParamDirective[T](valueParser: String => T):
  ConvertibleToDirective[HeaderParameter[Option[T]]] = (_: String, hp: HeaderParameter[Option[T]]) => {

    headerTemplate(
      () => optionalHeaderValueByName(hp.name).map(os => os.map(valueParser(_))),
      (default: Option[T]) => optionalHeaderValueByName(hp.name.name).map(extractIfPresent(valueParser, default)),
      (value: Option[T]) => enumCase(MissingHeaderRejection(hp.name.name), hp, value),
      hp)
  }

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

}
