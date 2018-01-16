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

import akka.http.scaladsl.server.Directives.{headerValueByName, optionalHeaderValueByName}
import akka.http.scaladsl.server.MissingHeaderRejection
import net.jtownson.swakka.coreroutegen.ConvertibleToDirective.instance
import net.jtownson.swakka.openapimodel._
import net.jtownson.swakka.coreroutegen._
import net.jtownson.swakka.openapiroutegen.RouteGenTemplates._

trait HeaderParamConverters {

  type HeaderParamConverter[U] = ConvertibleToDirective.Aux[HeaderParameter[U], U]

  implicit val stringReqHeaderConverter: HeaderParamConverter[String] =
    requiredHeaderParamDirective(s => s)

  implicit val stringOptHeaderConverter: HeaderParamConverter[Option[String]] =
    optionalHeaderParamDirective(s => s)

  implicit val floatReqHeaderConverter: HeaderParamConverter[Float] =
    requiredHeaderParamDirective(_.toFloat)

  implicit val floatOptHeaderConverter: HeaderParamConverter[Option[Float]] =
    optionalHeaderParamDirective(_.toFloat)

  implicit val doubleReqHeaderConverter: HeaderParamConverter[Double] =
    requiredHeaderParamDirective(_.toDouble)

  implicit val doubleOptHeaderConverter: HeaderParamConverter[Option[Double]] =
    optionalHeaderParamDirective(_.toDouble)

  implicit val booleanReqHeaderConverter: HeaderParamConverter[Boolean] =
    requiredHeaderParamDirective(_.toBoolean)

  implicit val booleanOptHeaderConverter: HeaderParamConverter[Option[Boolean]] =
    optionalHeaderParamDirective(_.toBoolean)

  implicit val intReqHeaderConverter: HeaderParamConverter[Int] =
    requiredHeaderParamDirective(_.toInt)

  implicit val intOptHeaderConverter: HeaderParamConverter[Option[Int]] =
    optionalHeaderParamDirective(_.toInt)

  implicit val longReqHeaderConverter: HeaderParamConverter[Long] =
    requiredHeaderParamDirective(_.toLong)

  implicit val longOptHeaderConverter: HeaderParamConverter[Option[Long]] =
    optionalHeaderParamDirective(_.toLong)

  private def requiredHeaderParamDirective[T](valueParser: String => T):
  HeaderParamConverter[T] =
    instance((_: String, hp: HeaderParameter[T]) => {
    headerTemplate(
      () => headerValueByName(hp.name).map(valueParser(_)),
      (default: T) => optionalHeaderValueByName(hp.name).map(extractIfPresent(valueParser, default)),
      hp
    )
  })

  private def optionalHeaderParamDirective[T](valueParser: String => T):
  HeaderParamConverter[Option[T]] =
    instance((_: String, hp: HeaderParameter[Option[T]]) => {

    headerTemplate(
      () => optionalHeaderValueByName(hp.name).map(os => os.map(valueParser(_))),
      (default: Option[T]) => optionalHeaderValueByName(hp.name.name).map(extractIfPresent(valueParser, default)),
      hp)
  })

  private def extractIfPresent[T](valueParser: String => T, default: T)(maybeHeader: Option[String]): T =
    maybeHeader.fold(default)(valueParser)

  private def extractIfPresent[T](valueParser: String => T, default: Option[T])(maybeHeader: Option[String]): Option[T] =
    maybeHeader.fold(default)(header => Some(valueParser(header)))
}
