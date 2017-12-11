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
trait Invoker[Params] {
  type F
  type O

  def apply(f: F, l: Params): O
}

object Invoker {

  type Aux[L, FF, OO] = Invoker[L] {type F = FF; type O = OO}
  type AkkaHttpInvoker[L, F] = Invoker.Aux[L, F, Route]

  def apply[L](implicit invoker: Invoker[L]): Aux[L, invoker.F, invoker.O] = invoker

  implicit def invoker[Params, RawParams, FF, OO]
  (implicit
   pv: ParameterValue.Aux[Params, RawParams],
   fp: FnToProduct.Aux[FF, RawParams => OO]
  ): Invoker.Aux[Params, FF, OO] =
    new Invoker[Params] {
      type F = FF
      type O = OO

      override def apply(f: F, l: Params): O = {
        fp(f)(pv.get(l))
      }
    }
}
