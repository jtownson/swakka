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
package net.jtownson.swakka.coreroutegen

import akka.http.scaladsl.server.Directive1

/**
  * ConvertibleToDirective is a type class supporting the conversion
  * of parameter entities (query params, path params, etc) into Akka-Http directives
  * that extract the values of those entities.
  * These Directives are composed by RouteGen into a single Route.
  * The T type param is the parameter entity type (e.g. QueryParameter[Int])
  * The U type param is the extraction entity type (e.g. Int)
  */
trait ConvertibleToDirective[T] {
  type U
  def convertToDirective(modelPath: String, t: T): Directive1[U]
}

object ConvertibleToDirective {
  type Aux[T, UU] = ConvertibleToDirective[T]{ type U = UU }

  def instance[T, UU](f: (String, T) => Directive1[UU]): ConvertibleToDirective.Aux[T, UU] =
    new ConvertibleToDirective[T] {
      type U = UU

      override def convertToDirective(modelPath: String, t: T): Directive1[UU] = f(modelPath, t)
    }

  def converter[T, U](implicit ev: Aux[T, U]): ConvertibleToDirective.Aux[T, U] = ev
}