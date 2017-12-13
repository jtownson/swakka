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

import akka.http.scaladsl.server.{Directive1, MalformedQueryParamRejection, Rejection}
import akka.http.scaladsl.server.Directives.{onComplete, provide, reject}
import akka.http.scaladsl.server.directives.BasicDirectives.extract
import akka.http.scaladsl.unmarshalling.{FromStringUnmarshaller, Unmarshal}
import akka.stream.Materializer
import net.jtownson.swakka.coreroutegen.ConvertibleToDirective.instance
import net.jtownson.swakka.openapimodel._
import net.jtownson.swakka.coreroutegen._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

trait MultiParamConverters {

  type MultiParamConverter[T, U <: Parameter[T]] = ConvertibleToDirective.Aux[MultiValued[T, U], Seq[T]]

  implicit def multiValuedConverter[T, U <: QueryParameter[T]]
  (implicit um: FromStringUnmarshaller[T], mat: Materializer, ec: ExecutionContext):
  MultiParamConverter[T, U] =
    instance((_: String, mp: MultiValued[T, U]) => {

      val marshalledParams: Directive1[Try[Seq[T]]] = queryParamsWithName(mp.name.name).
        map(params => Future.sequence(params.map(param => Unmarshal(param).to[T]))).
        flatMap(marshalledParams => onComplete(marshalledParams))

      marshalledParams.flatMap({
        case Success(Nil) => mp.singleParam.default match {
          case Some(default) => provideWithCheck(Seq(default), mp)
          case _ => mp.default match {
            case Some(defaultSeq) => provideWithCheck(defaultSeq, mp)
            case _ => provideWithCheck(Nil, mp)
          }
        }
        case Success(seq) => provideWithCheck(seq, mp)
        case Failure(t) => reject(
          MalformedQueryParamRejection(mp.name.name,
            s"Failed to marshal multivalued parameter ${mp.name.name}. The following error occurred: $t",
            Some(t)))
      })
    })

  private def provideWithCheck[T, U <: Parameter[T]](s: Seq[T], p: MultiValued[T, U]): Directive1[Seq[T]] =
    provideWithCheck(s, p, parameterRejection(s, p))

  private def provideWithCheck[T, U <: Parameter[T]](values: Seq[T], p: MultiValued[T, U], errHandler: => Rejection): Directive1[Seq[T]] = {
    p.singleParam.enum match {
      case None => provide(values)
      case Some(enum) if values.forall(enum.contains) => provide(values)
      case _ => reject(errHandler)
    }
  }

  private def parameterRejection[T, U <: Parameter[T]](s: Seq[T], p: MultiValued[T, U]): Rejection =
    MalformedQueryParamRejection(
      p.name.name,
      s"The value $s is not allowed by this request. They are limited to ${p.enum}.")

  private def queryParamsWithName(name: String): Directive1[Seq[String]] =
    extract(_.request.uri.query().toSeq).
      map(_.filter( {case (key, _) => name == key} ).
      map(_._2)).
      flatMap(params => provide(params))

}