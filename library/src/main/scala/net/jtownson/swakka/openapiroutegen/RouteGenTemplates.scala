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

import akka.http.scaladsl.server.{Directive1, MalformedQueryParamRejection, Rejection, ValidationRejection}
import akka.http.scaladsl.server.Directives.{provide, reject}
import net.jtownson.swakka.openapimodel._
import net.jtownson.swakka.openapimodel.HeaderParameter._
import net.jtownson.swakka.openapimodel.QueryParameter._
import net.jtownson.swakka.openapimodel.FormFieldParameter._
import net.jtownson.swakka.openapimodel.BodyParameter._
import net.jtownson.swakka.openapimodel.PathParameter._
import net.jtownson.swakka.openapimodel.MultiValued._


object RouteGenTemplates {

  def headerTemplate[T](fNoDefault: () => Directive1[T],
                                fDefault: T => Directive1[T],
                                fEnum: T => Directive1[T],
                                hp: HeaderParameter[T]): Directive1[T] = {

    hp.default match {
      case Some(default) =>
        fDefault(default).flatMap(fEnum)
      case None =>
        fNoDefault().flatMap(fEnum)
    }
  }

  def enumCase[T](rejection: Rejection, hp: HeaderParameter[T], value: T): Directive1[T] = {
    hp.enum match {
      case None => provide(value)
      case Some(seq) if seq.contains(value) => provide(value)
      case _ => reject(rejection)
    }
  }


  def parameterTemplate[T](fNoDefault: () => Directive1[T],
                                   fDefault: T => Directive1[T],
                                   fEnum: T => Directive1[T],
                                   qp: QueryParameter[T]): Directive1[T] = {

    qp.default match {
      case Some(default) =>
        fDefault(default).flatMap(fEnum)
      case None =>
        fNoDefault().flatMap(fEnum)
    }
  }

  def enumCase[T](qp: QueryParameter[T], value: T): Directive1[T] = {
    qp.enum match {
      case None => provide(value)
      case Some(seq) if seq.contains(value) => provide(value)
      case _ => reject(MalformedQueryParamRejection(qp.name.name, enumErrMsg(value, qp)))
    }
  }

  private def enumErrMsg[T](value: T, qp: QueryParameter[T]): String =
    s"The parameter value $value is not allowed by this request. They are limited to ${qp.enum}."


  def formFieldTemplate[T](fNoDefault: () => Directive1[T],
                           fDefault: T => Directive1[T],
                           fEnum: T => Directive1[T],
                           fp: FormFieldParameter[T]): Directive1[T] = {

    fp.default match {
      case Some(default) =>
        fDefault(default).flatMap(fEnum)
      case None =>
        fNoDefault().flatMap(fEnum)
    }
  }

  def enumCase[T](fp: FormFieldParameter[T], value: T): Directive1[T] = {
    fp.enum match {
      case None => provide(value)
      case Some(seq) if seq.contains(value) => provide(value)
      case _ => reject(MalformedQueryParamRejection(fp.name.name, enumErrMsg(value, fp)))
    }
  }

  private def enumErrMsg[T](value: T, fp: FormFieldParameter[T]): String =
    s"The form field value $value is not allowed by this request. They are limited to ${fp.enum}."

  def enumCase[T](bp: BodyParameter[T])(value: T): Directive1[T] = bp.enum match {
    case None => provide(value)
    case Some(seq) if seq.contains(value) => provide(value)
    case _ => reject(ValidationRejection(s"The request body $value is not allowed by this request. They are limited to ${bp.enum}"))
  }

  def enumCase[T](pp: PathParameter[T])(value: T): Directive1[T] = pp.enum match {
    case None => provide(value)
    case Some(seq) if seq.contains(value) => provide(value)
    case _ => reject(ValidationRejection(s"The path value $value is not allowed by this request. They are limited to ${pp.enum}"))
  }

}