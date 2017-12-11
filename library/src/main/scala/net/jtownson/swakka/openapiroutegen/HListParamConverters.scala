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

import akka.http.scaladsl.server.Directive1
import akka.http.scaladsl.server.Directives.{pass, path}
import net.jtownson.swakka.openapiroutegen.PathHandling.containsParamToken
import shapeless.{::, HList, HNil}

trait HListParamConverters {

  implicit val hNilConverter: ConvertibleToDirective[HNil] =
    (modelPath: String, _: HNil) => {
      if (containsParamToken(modelPath))
        pass.tmap[HNil](_ => HNil)
      else
        path(PathHandling.splittingPathMatcher(modelPath)).tmap[HNil](_ => HNil)
    }

  implicit def hConsConverter[H, T <: HList](implicit head: ConvertibleToDirective[H],
                                             tail: ConvertibleToDirective[T]): ConvertibleToDirective[H :: T] =
    (modelPath: String, l: H :: T) => {
      val headDirective: Directive1[H] = head.convertToDirective(modelPath, l.head)
      val tailDirective: Directive1[T] = tail.convertToDirective(modelPath, l.tail)

      (headDirective & tailDirective).tmap((t: (H, T)) => t._1 :: t._2)
    }
}
