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

import akka.http.scaladsl.server.Directives.{as, entity}
import akka.http.scaladsl.unmarshalling.FromRequestUnmarshaller
import net.jtownson.swakka.openapimodel._
import AdditionalDirectives._
import RouteGenTemplates._

trait BodyParamConverters {

  implicit def bodyParamConverter[T](implicit ev: FromRequestUnmarshaller[T]): ConvertibleToDirective[BodyParameter[T]] =
    (_: String, bp: BodyParameter[T]) => {
      bp.default match {
        case None =>
          entity(as[T]).flatMap(enumCase(bp)).map(close(bp))
        case Some(default) =>
          optionalEntity[T](as[T]).map(_.getOrElse(default)).flatMap(enumCase(bp)).map(close(bp))
      }
    }

  implicit def bodyOptParamConverter[T](implicit ev: FromRequestUnmarshaller[T]): ConvertibleToDirective[BodyParameter[Option[T]]] =
    (_: String, bp: BodyParameter[Option[T]]) => {
      bp.default match {
        case None =>
          optionalEntity[T](as[T]).flatMap(enumCase(bp)).map(close(bp))
        case Some(default) =>
          optionalEntity[T](as[T]).map(returnOrElse(default)).flatMap(enumCase(bp)).map(close(bp))
      }
    }

  private def returnOrElse[T](default: Option[T])(maybeEntity: Option[T]) = maybeEntity match {
    case v@Some(_) => v
    case _ => default
  }
}
