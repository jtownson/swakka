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

import akka.http.scaladsl.server.Directives.{
  DoubleNumber,
  provide,
  reject,
  rawPathPrefixTest
}
import akka.http.scaladsl.server.PathMatchers.{IntNumber, LongNumber, Segment}
import akka.http.scaladsl.server.{Directive1, PathMatcher1, ValidationRejection}
import net.jtownson.swakka.coreroutegen.ConvertibleToDirective.instance
import net.jtownson.swakka.coreroutegen._
import net.jtownson.swakka.openapimodel._
import net.jtownson.swakka.openapiroutegen.ParamValidator._
import net.jtownson.swakka.openapiroutegen.PathHandling.pathWithParamMatcher

trait PathParamConstrainedConverters {

  type PathParamConstrainedConverter[T] =
    ConvertibleToDirective.Aux[PathParameterConstrained[T, T], T]

  implicit val stringReqPathConverterConstrained
    : PathParamConstrainedConverter[String] =
    pathParamDirective(Segment, stringValidator)

  implicit val floatPathConverterConstrained
    : PathParamConstrainedConverter[Float] =
    pathParamDirective(FloatNumber, numberValidator[Float])

  implicit val doublePathConverterConstrained
    : PathParamConstrainedConverter[Double] =
    pathParamDirective(DoubleNumber, numberValidator[Double])

  implicit val booleanPathConverterConstrained
    : PathParamConstrainedConverter[Boolean] =
    pathParamDirective(BooleanSegment, anyValidator[Boolean])

  implicit val intPathConverterConstrained: PathParamConstrainedConverter[Int] =
    pathParamDirective(IntNumber, integralValidator[Int])

  implicit val longPathConverterConstrained
    : PathParamConstrainedConverter[Long] =
    pathParamDirective(LongNumber, integralValidator[Long])

  private def pathParamDirective[T](
      pm: PathMatcher1[T],
      validator: ParamValidator[T, T]): PathParamConstrainedConverter[T] =
    instance(
      (modelPath: String, pp: PathParameterConstrained[T, T]) =>
        rawPathPrefixTest(pathWithParamMatcher(modelPath, pp.name.name, pm))
          .flatMap(
            (t: T) =>
              validator
                .validate(pp.constraints, t)
                .fold(errors => rejectWithValidationErrors(errors),
                      value => provide(value))))

  private def rejectWithValidationErrors[T](
      validationErrors: String): Directive1[T] =
    reject(ValidationRejection(validationErrors))

}
