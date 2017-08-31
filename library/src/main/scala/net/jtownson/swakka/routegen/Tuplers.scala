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

trait Tuplers {

  implicit def tupler[P1, T](f: (P1) => T): Tuple1[P1] => T =
    t => f(t._1)

  implicit def tupler[P1, P2, T](f: (P1, P2) => T): ((P1, P2)) => T =
    f.tupled

  implicit def untupler[P1, P2, T](f: ((P1, P2)) => T): (P1, P2) => T =
    (p1, p2) => f(Tuple2(p1, p2))

}

object Tuplers extends Tuplers
