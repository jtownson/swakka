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

import akka.http.scaladsl.model.HttpHeader
import akka.http.scaladsl.server.{Directive1, Rejection, ValidationRejection}
import akka.http.scaladsl.server.Directives.{onComplete, provide, reject}
import akka.http.scaladsl.server.directives.BasicDirectives.extract
import akka.http.scaladsl.server.directives.HeaderDirectives._
import akka.http.scaladsl.server.directives.FormFieldDirectives._
import akka.http.scaladsl.unmarshalling.{FromStringUnmarshaller, Unmarshal}
import akka.stream.Materializer
import net.jtownson.swakka.coreroutegen.ConvertibleToDirective.instance
import net.jtownson.swakka.openapimodel._
import net.jtownson.swakka.coreroutegen._
import net.jtownson.swakka.openapiroutegen.ParamValidator._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

trait MultiParamConverters {

  type MultiParamConverter[T, P <: Parameter[T]] =
    ConvertibleToDirective.Aux[MultiValued[T, P], Seq[T]]

  trait ConstraintShim[T, U, P <: Parameter[T]] {
    def constraints(param: P): Option[Constraints[U]]
  }

  object ConstraintShim {
    def apply[T, U, P <: Parameter[T]](f: P => Option[Constraints[U]]) =
      new ConstraintShim[T, U, P] {
        override def constraints(param: P) = f(param)
      }

    implicit def qpch[T]: ConstraintShim[T, T, QueryParameter[T]] = ConstraintShim(_ => None)
    implicit def qpcch[T] = ConstraintShim[T, T, QueryParameterConstrained[T, T]](p => Some(p.constraints))

    implicit def hpch[T]: ConstraintShim[T, T, HeaderParameter[T]] = ConstraintShim(_ => None)
    implicit def hpcch[T]: ConstraintShim[T, T, HeaderParameterConstrained[T, T]] = ConstraintShim(p => Some(p.constraints))

    implicit def ffpch[T]: ConstraintShim[T, T, FormFieldParameter[T]] = ConstraintShim(_ => None)
    implicit def ffpcch[T]: ConstraintShim[T, T, FormFieldParameterConstrained[T, T]] = ConstraintShim(p => Some(p.constraints))

    // Array value path parameters are apparently in the swagger spec,
    // but would anybody split their url paths with pipes or commas??!
    // Leaving out for now.
  }

  implicit val implicitStringValidator: ParamValidator[String, String] = stringValidator
  implicit val implicitIntValidator: ParamValidator[Int, Int] = integralValidator
  implicit val implicitLongValidator: ParamValidator[Long, Long] = integralValidator
  implicit val implicitFloatValidator: ParamValidator[Float, Float] = numberValidator
  implicit val implicitDoubleValidator: ParamValidator[Double, Double] = numberValidator
  implicit val implicitBooleanValidator: ParamValidator[Boolean, Boolean] = anyValidator

  implicit def implicitOptionValidator[T](implicit ev: ParamValidator[T, T]): ParamValidator[Option[T], T] = optionValidator(ev)
  implicit def implicitSequenceValidator[T](implicit ev: ParamValidator[T, T]): ParamValidator[Seq[T], T] = sequenceValidator(ev)

  implicit def multiValuedQueryParamConverter[T](
      implicit um: FromStringUnmarshaller[T],
      mat: Materializer,
      ec: ExecutionContext,
      validator: ParamValidator[Seq[T], T],
      ch: ConstraintShim[T, T, QueryParameter[T]]): MultiParamConverter[T, QueryParameter[T]] =
    instance((_: String, mp: MultiValued[T, QueryParameter[T]]) =>
      marshallParams(queryParamsWithName(mp.name.name, mp.collectionFormat)).flatMap(validateAndProvide(mp, validator, ch)))

  implicit def multiValuedQueryParamConstrainedConverter[T, U](
      implicit um: FromStringUnmarshaller[T],
      mat: Materializer,
      ec: ExecutionContext,
      validator: ParamValidator[Seq[T], U],
      ch: ConstraintShim[T, U, QueryParameterConstrained[T, U]]): MultiParamConverter[T, QueryParameterConstrained[T, U]] =
    instance((_: String, mp: MultiValued[T, QueryParameterConstrained[T, U]]) =>
      marshallParams(queryParamsWithName(mp.name.name, mp.collectionFormat)).flatMap(validateAndProvide(mp, validator, ch)))

  implicit def multiValuedHeaderParamConverter[T](
      implicit um: FromStringUnmarshaller[T],
      mat: Materializer,
      ec: ExecutionContext,
      validator: ParamValidator[Seq[T], T],
      ch: ConstraintShim[T, T, HeaderParameter[T]]): MultiParamConverter[T, HeaderParameter[T]] =
    instance((_: String, mp: MultiValued[T, HeaderParameter[T]]) =>
      marshallParams(headerParamsWithName(mp.name.name, mp.collectionFormat)).flatMap(validateAndProvide(mp, validator, ch)))

  implicit def multiValuedHeaderParamConstrainedConverter[T, U](
      implicit um: FromStringUnmarshaller[T],
      mat: Materializer,
      ec: ExecutionContext,
      validator: ParamValidator[Seq[T], U],
      ch: ConstraintShim[T, U, HeaderParameterConstrained[T, U]]): MultiParamConverter[T, HeaderParameterConstrained[T, U]] =
    instance((_: String, mp: MultiValued[T, HeaderParameterConstrained[T, U]]) =>
      marshallParams(headerParamsWithName(mp.name.name, mp.collectionFormat)).flatMap(validateAndProvide(mp, validator, ch)))

  implicit def multiValuedFormFieldParamConverter[T](
      implicit um: FromStringUnmarshaller[T],
      mat: Materializer,
      ec: ExecutionContext,
      validator: ParamValidator[Seq[T], T],
      ch: ConstraintShim[T, T, FormFieldParameter[T]]): MultiParamConverter[T, FormFieldParameter[T]] =
    instance((_: String, mp: MultiValued[T, FormFieldParameter[T]]) =>
      marshallParams(formFieldParamsWithName(mp.name.name, mp.collectionFormat)).flatMap(validateAndProvide(mp, validator, ch)))

  implicit def multiValuedFormFieldParamConstrainedConverter[T, U](
      implicit um: FromStringUnmarshaller[T],
      mat: Materializer,
      ec: ExecutionContext,
      validator: ParamValidator[Seq[T], U],
      ch: ConstraintShim[T, U, FormFieldParameterConstrained[T, U]]): MultiParamConverter[T, FormFieldParameterConstrained[T, U]] =
    instance((_: String, mp: MultiValued[T, FormFieldParameterConstrained[T, U]]) =>
      marshallParams(formFieldParamsWithName(mp.name.name, mp.collectionFormat)).flatMap(validateAndProvide(mp, validator, ch)))

  private def marshallParams[T](paramExtraction: Directive1[Seq[String]])(implicit um: FromStringUnmarshaller[T],
                                                                     mat: Materializer,
                                                                     ec: ExecutionContext): Directive1[Try[Seq[T]]] =
    paramExtraction
      .map(params =>
        Future.sequence(params.map(param => Unmarshal(param).to[T])))
      .flatMap(marshalledParams => onComplete(marshalledParams))

  private def validateAndProvide[T, U, P <: Parameter[T]]
  (mp: MultiValued[T, P], validator: ParamValidator[Seq[T], U], ch: ConstraintShim[T, U, P])(tst: Try[Seq[T]]): Directive1[Seq[T]] =
    tst match {
      case Success(Nil) =>
        mp.singleParam.default match {
          case Some(default) =>
            provideWithCheck(Seq(default), mp, validator, ch)
          case _ =>
            mp.default match {
              case Some(defaultSeq) =>
                provideWithCheck(defaultSeq, mp, validator, ch)
              case _ => provideWithCheck(Nil, mp, validator, ch)
            }
        }
      case Success(seq) => provideWithCheck(seq, mp, validator, ch)
      case Failure(t) =>
        reject(ValidationRejection(
          s"Failed to marshal multivalued parameter ${mp.name.name}. The following error occurred: $t",
          Some(t)))
    }

  private def provideWithCheck[T, P <: Parameter[T], U](
      values: Seq[T],
      p: MultiValued[T, P],
      validator: ParamValidator[Seq[T], U],
      constraintHack: ConstraintShim[T, U, P]): Directive1[Seq[T]] = {

    val maybeConstraints: Option[Constraints[U]] =
      constraintHack.constraints(p.singleParam)
    val successDefault = Right(values)

    val validationResult: Either[String, Seq[T]] = maybeConstraints
      .map(c => validator.validate(c, values))
      .getOrElse(successDefault)

    validationResult.fold(
      errors => reject(validationRejection(values, p.name.name, errors)),
      validatedValues => provide(validatedValues))
  }

  private def validationRejection[T](
      s: Seq[T],
      paramName: String,
      errMsg: String): Rejection =
    ValidationRejection(
      s"The value $s is not allowed by this request for parameter $paramName. $errMsg.")

  private val delimitedFormats: Set[CollectionFormat] = Set(pipes, tsv, csv, ssv)

  private def queryParamsWithName(
      name: String,
      collectionFormat: CollectionFormat): Directive1[Seq[String]] =
    collectionFormat match {
      case format if format == multi =>
        extract(_.request.uri.query().toSeq)
          .map(_.filter({ case (key, _) => name == key }).map(_._2))
          .flatMap(params => provide(params))
      case format if delimitedFormats.contains(format) =>
        extract(_.request.uri.query().toSeq)
          .map(_.find({ case (key, _) => name == key }).map(_._2))
          .flatMap(
            maybeParam => {
              lazy val rejection: Directive1[Seq[String]] = reject(validationRejection(Nil, name, "The parameter is required but missing"))
              val provision: String => Directive1[Seq[String]] = param => provide(parse(param, format.delimiter))

              maybeParam.fold(rejection)(provision)
            }
          )
    }

  private def headerParamsWithName(
      name: String,
      collectionFormat: CollectionFormat): Directive1[Seq[String]] =
    collectionFormat match {
      case format if format == multi => {
        extract(_.request.headers)
          .map(_.filter({ case HttpHeader(key, _) => key == name.toLowerCase }).map(_.value))
            .flatMap(headerValues => provide(headerValues))
      }
      case format if delimitedFormats.contains(format) =>
        optionalHeaderValueByName(name).flatMap(maybeHeader => {
          lazy val rejection: Directive1[Seq[String]] = reject(validationRejection(Nil, name, s"The header $name is required but missing"))
          val provision: String => Directive1[Seq[String]] = header => provide(parse(header, format.delimiter))
          maybeHeader.fold(rejection)(provision)
        })
    }

  private def formFieldParamsWithName(
      name: String,
      collectionFormat: CollectionFormat): Directive1[Seq[String]] =
    collectionFormat match {
      case format if format == multi =>
        formFields(name.as[String].*).map(_.toSeq)
      case format if delimitedFormats.contains(format) =>
        formField(name.as[String]).map(value => parse(value, format.delimiter))
    }

  private def parse(param: String, delimiter: Char): Seq[String] =
    param.split(delimiter)
}
