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

import akka.http.scaladsl.server.{Directive1, MalformedQueryParamRejection, MissingQueryParamRejection}
import akka.http.scaladsl.server.Directives.{onComplete, provide, reject}
import akka.http.scaladsl.server.directives.BasicDirectives.extract
import akka.http.scaladsl.unmarshalling.{FromStringUnmarshaller, Unmarshal}
import akka.stream.Materializer
import net.jtownson.swakka.model.Parameters.MultiValued.OpenMultiValued
import net.jtownson.swakka.model.Parameters.{MultiValued, Parameter, QueryParameter}

import scala.collection.immutable.Seq
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}
trait MultiParamConverters {

  implicit def multiValuedConverter[T, U <: QueryParameter[T]](implicit um: FromStringUnmarshaller[T], mat: Materializer, ec: ExecutionContext):
  ConvertibleToDirective[MultiValued[T, U]] =
    (_: String, mp: MultiValued[T, U]) => {

      val marshalledParams: Directive1[Try[Seq[T]]] = queryParamsWithName(mp.name.name).
        map(params => Future.sequence(params.map(param => Unmarshal(param).to[T]))).
        flatMap(marshalledParams => onComplete(marshalledParams))

      marshalledParams.flatMap({
        case Success(seq) => provide(close(mp)(seq))
        case Failure(t) => reject(
          MalformedQueryParamRejection(mp.name.name,
            s"Failed to marshal multivalued parameter ${mp.name.name}. The following error occurred: $t",
            Some(t)))
      })
    }

  private def queryParamsWithName(name: String): Directive1[Seq[String]] =
    extract(_.request.uri.query().toSeq).
      map(_.filter( {case (key, _) => name == key} ).
      map(_._2)).
      flatMap(params => if (params.isEmpty) reject(MissingQueryParamRejection(name)) else provide(params))

  private def close[T, U <: Parameter[T]](mp: MultiValued[T, U]): Seq[T] => MultiValued[T, U] =
    t => mp.asInstanceOf[OpenMultiValued[T, U]].closeWith(t)
}
