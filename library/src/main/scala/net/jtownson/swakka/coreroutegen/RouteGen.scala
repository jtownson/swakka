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

import akka.http.scaladsl.server._

/**
  * RouteGen is a type class that supports the conversion of an OpenApi model into a Akka-Http Route.
  * This allows the processing of an HTTP request according to a Swagger definition.
  * See also ConvertibleToDirective.
  *
  * @tparam T
  */
trait RouteGen[T] {
  def toRoute(t: T): Route
}

object RouteGen {
  def apply[T](f: T => Route): RouteGen[T] = new RouteGen[T] {
    override def toRoute(t: T): Route = f(t)
  }
}