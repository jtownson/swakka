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

import akka.http.scaladsl.server.Route
import shapeless.ops.function.FnToProduct

/**
  * Invoker provides the type plumbing to map the type of an input HList of Parameter[T]
  * to that of a function accepting an argument list of the inner types of those Parameters.
  * For eg. if the input HList is
  * QueryParameter[String] :: QueryParameter[Int] :: PathParameter[Boolean] :: HNil
  * Invoker will define a dependent Function type
  * (String, Int, Boolean) => R
  * and call that function, returning the result, R (also a dependent type).
  *
  * AkkHttpInvoker is a specialization of Invoker where the return type of F
  * is an akka Route.
  */
trait Invoker[RequestParams] {
  type EndpointParams
  type F
  type O

  def apply(f: F, l: EndpointParams): O
}

object Invoker {

  type Aux[RequestParams, EPs, FF, OO] = Invoker[RequestParams] {
    type EndpointParams = EPs;
    type F = FF;
    type O = OO
  }

  type AkkaHttpInvoker[RequestParams, EndpointParams, F] = Invoker.Aux[RequestParams, EndpointParams, F, Route]

  def apply[RequestParams](implicit invoker: Invoker[RequestParams])
  : Aux[RequestParams, invoker.EndpointParams, invoker.F, invoker.O] = invoker

  implicit def invoker[RequestParams, EPs, FF, OO]
  (implicit fp: FnToProduct.Aux[FF, EPs => OO]): Invoker.Aux[RequestParams, EPs, FF, OO] =
    new Invoker[RequestParams] {
      type EndpointParams = EPs
      type F = FF
      type O = OO

      override def apply(f: FF, l: EndpointParams) = {
        fp(f)(l)
      }
    }
}
