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

package net.jtownson.swakka.jsonschema

import shapeless.labelled.KeyTag
import shapeless.tag.Tagged

import shapeless.ops.hlist.{RightFolder, ToTraversable}
import shapeless.ops.record.Keys
import shapeless.{HList, HNil, LabelledGeneric, Poly2}

/**
  * Thanks to Radu @ https://github.com/radusw
  * See https://stackoverflow.com/questions/47360597/type-level-filtering-using-shapeless/47367952#47367952
  */
trait FieldNameExtractor[T] {

  /**
    * Extracts filtered field names for type [[T]],
    * given a polymorphic function that acts as the type filter
    */
  def extract[L <: HList, R <: HList, O <: HList](op: Poly2)(
    implicit lgen: LabelledGeneric.Aux[T, L],
    folder: RightFolder.Aux[L, HNil.type, op.type, R],
    keys: Keys.Aux[R, O],
    traversable: ToTraversable.Aux[O, List, Symbol]
  ): List[String] = {
    val result = keys().to[List]
    result.map(_.name)
  }
}

object FieldNameExtractor {

  def apply[T] = new FieldNameExtractor[T] {}

  type FilterO[A, T] = Option[A] with KeyTag[Symbol with Tagged[T], Option[A]]

  trait Ignore extends Poly2 {
    implicit def default[A, L <: HList] = at[A, L]((_, l) => l)
  }

  trait Accept extends Poly2 {
    implicit def default[A, L <: HList] = at[A, L](_ :: _)
  }

  object optional extends Ignore {
    implicit def option[A, T, L <: HList] = at[FilterO[A, T], L](_ :: _)
  }

  object nonOptional extends Accept {
    implicit def option[A, T, L <: HList] = at[FilterO[A, T], L]((_, l) => l)
  }
}
