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

import akka.http.scaladsl.server.Directives.{DoubleNumber, rawPathPrefixTest}
import akka.http.scaladsl.server.{PathMatcher, PathMatcher1}
import net.jtownson.swakka.OpenApiModel._
import net.jtownson.swakka.routegen.PathHandling.pathWithParamMatcher
import akka.http.scaladsl.server.PathMatchers.{IntNumber, LongNumber, Segment}
import RouteGenTemplates._

trait PathParamConverters {

  val BooleanSegment: PathMatcher1[Boolean] =
    PathMatcher("""^(?i)(true|false)$""".r) flatMap (s => Some(s.toBoolean))

  val FloatNumber: PathMatcher1[Float] =
    PathMatcher("""[+-]?\d*\.?\d*""".r) flatMap { string =>
      try Some(java.lang.Float.parseFloat(string))
      catch {
        case _: NumberFormatException ⇒ None
      }
    }

  implicit val stringReqPathConverter: ConvertibleToDirective[PathParameter[String]] =
    pathParamDirective(Segment)

  implicit val floatPathConverter: ConvertibleToDirective[PathParameter[Float]] =
    pathParamDirective(FloatNumber)

  implicit val doublePathConverter: ConvertibleToDirective[PathParameter[Double]] =
    pathParamDirective(DoubleNumber)

  implicit val booleanPathConverter: ConvertibleToDirective[PathParameter[Boolean]] =
    pathParamDirective(BooleanSegment)

  implicit val intPathConverter: ConvertibleToDirective[PathParameter[Int]] =
    pathParamDirective(IntNumber)

  implicit val longPathConverter: ConvertibleToDirective[PathParameter[Long]] =
    pathParamDirective(LongNumber)

  private def pathParamDirective[T](pm: PathMatcher1[T]): ConvertibleToDirective[PathParameter[T]] = {
    (modelPath: String, pp: PathParameter[T]) =>
      rawPathPrefixTest(pathWithParamMatcher(modelPath, pp.name.name, pm)).
        flatMap(enumCase(pp)).
        map(close(pp))
  }
}
