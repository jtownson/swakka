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

import shapeless.{::, HList, HNil}

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

/**
  * ParameterValue is a trait that works in combination with Invoker.
  * As its name suggests, it's role is to extract the values of parameters
  * to that Invoker can pass these to the endpoint function.
  * @tparam P
  */
trait ParameterValue[P] {
  type Out

  def get(p: P): Out
}

object ParameterValue {
  type Aux[P, O] = ParameterValue[P] {type Out = O}

  def apply[P](implicit inst: ParameterValue[P]): Aux[P, inst.Out] = inst

  def instance[P, O](f: P => O): Aux[P, O] = new ParameterValue[P] {
    type Out = O

    override def get(p: P) = f(p)
  }

  implicit val hNilParameterValue: Aux[HNil, HNil] =
    instance(_ => HNil)

  implicit def hListParameterValue[H, T <: HList, HO, TO <: HList](
                                                                    implicit
                                                                    ph: Aux[H, HO],
                                                                    pt: Aux[T, TO]): Aux[H :: T, HO :: TO] =
    instance[H :: T, HO :: TO] {
      case (h :: t) => ph.get(h) :: pt.get(t)
    }
}