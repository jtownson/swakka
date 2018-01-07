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

import net.jtownson.swakka.openapimodel.Constraints

import scala.collection.immutable

trait ParamValidator[T, U] {
  def validate(constraints: Constraints[U], value: T): Either[String, T]
}

object ParamValidator {

  def anyValidator[T]: ParamValidator[T, T] = (constraints: Constraints[T], value: T) => {
    import constraints._

    val validationFailures: immutable.Seq[String] = List(
      enum.map(validateEnum(value))
    ).flatten.flatten

    if (validationFailures.isEmpty)
      Right(value)
    else
      Left(validationFailures.mkString(", "))
  }

  val stringValidator: ParamValidator[String, String] = (constraints: Constraints[String], value: String) => {

    import constraints._

    val validationFailures: Seq[String] = List(
      enum.flatMap(validateEnum(value)),
      maxLength.flatMap(validateMaxLength(value)),
      minLength.flatMap(validateMinLength(value)),
      pattern.flatMap(validatePattern(value))
    ).flatten // flatten the existence of a constraint + flatten the existence of an error against that constraint

    Either.cond(validationFailures.isEmpty, value, validationFailures.mkString(", "))
  }

  def numberValidator[T: Numeric]: ParamValidator[T, T] = (constraints: Constraints[T], value: T) => {

    import constraints._

    val validationFailures: Seq[String] = List(
      enum.flatMap(validateEnum(value)),
      maximum.flatMap(validateMaximum(value)),
      minimum.flatMap(validateMinimum(value)),
      exclusiveMaximum.flatMap(validateExclusiveMaximum(value)),
      exclusiveMinimum.flatMap(validateExclusiveMinimum(value))
    ).flatten

    Either.cond(validationFailures.isEmpty, value, validationFailures.mkString(", "))
  }

  def integralValidator[T: Integral]: ParamValidator[T, T] = (constraints: Constraints[T], value: T) => {

    val numberValidation: Either[String, T] = numberValidator[T].validate(constraints, value)

    val maybeValidationErr: Option[String] = constraints.multipleOf.flatMap(validateMultipleOf(value))

    val integralValidation: Either[String, T] = Either.cond(maybeValidationErr.isEmpty, value, maybeValidationErr.mkString)

    sequenceEither[T, T](Seq(numberValidation, integralValidation), value)
  }

  def optionValidator[T](innerValidator: ParamValidator[T, T]): ParamValidator[Option[T], T] = {
    (constraints: Constraints[T], value: Option[T]) =>
      value.map(innerValidator.validate(constraints, _).map(Option(_))).getOrElse(Right(value))
  }

  def sequenceValidator[T](innerValidator: ParamValidator[T, T]): ParamValidator[Seq[T], T] =
    (constraints: Constraints[T], value: Seq[T]) => {
      sequenceEither[T, Seq[T]](value.map(innerValidator.validate(constraints, _)), value)
    }

  private def sequenceEither[T, U](eithers: Seq[Either[String, T]], value: U): Either[String, U] = {
    val errors: Seq[String] = eithers.collect({case Left(err) => err})
    if (errors.isEmpty)
      Right(value)
    else
      Left(errors.mkString(", "))
  }

  private def validateEnum[T](value: T)(enum: Set[T]): Option[String] =
    if (enum.contains(value))
      None
    else
      Some(s"""The value "$value" is not in the specified enum [${enum.mkString(", ")}]""")

  private def validateMaxLength(value: String)(maxLength: Long): Option[String] =
    if (value.length <= maxLength)
      None
    else
      Some(s"""The value "$value" is longer than the maxLength of $maxLength""")

  private def validateMinLength(value: String)(minLength: Int): Option[String] =
    if (value.length >= minLength)
      None
    else
      Some(s"""The value "$value" is shorter than the minLength of $minLength""")

  private def validatePattern(value: String)(pattern: String): Option[String] = {
    val regex = raw"$pattern".r
    value match {
      case regex(_*) => None
      case _ => Some(s"""The value "$value" does not match pattern "$pattern"""")
    }
  }

  private def validateMultipleOf[T](value: T)(multipleOf: T)(implicit ev: Integral[T]): Option[String] = {
    if (ev.rem(value, multipleOf) == 0)
      None
    else
      Some(s"""The value $value is not a multiple of $multipleOf""")
  }

  private def validateMaximum[T](value: T)(maximum: T)(implicit ev: Numeric[T]): Option[String] = {
    if (ev.lteq(value, maximum))
      None
    else
      Some(s"""The value $value is larger than the maximum of $maximum""")
  }

  private def validateMinimum[T](value: T)(minimum: T)(implicit ev: Numeric[T]): Option[String] = {
    if (ev.gteq(value, minimum))
      None
    else
      Some(s"""The value $value is smaller than the minimum of $minimum""")
  }

  private def validateExclusiveMaximum[T](value: T)(exclusiveMaximum: T)(implicit ev: Numeric[T]): Option[String] = {
    if (ev.lt(value, exclusiveMaximum))
      None
    else
      Some(s"""The value $value is not smaller than the exclusive maximum of $exclusiveMaximum""")
  }

  private def validateExclusiveMinimum[T](value: T)(exclusiveMinimum: T)(implicit ev: Numeric[T]): Option[String] = {
    if (ev.gt(value, exclusiveMinimum))
      None
    else
      Some(s"""The value $value is not larger than the exclusive minimum of $exclusiveMinimum""")
  }
}
