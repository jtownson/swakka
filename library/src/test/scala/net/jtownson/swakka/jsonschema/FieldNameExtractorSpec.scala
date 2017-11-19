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


import net.jtownson.swakka.jsonschema.FieldNameExtractor.{optional, nonOptional}
import org.scalatest.FlatSpec
import org.scalatest.Matchers._

class FieldNameExtractorSpec extends FlatSpec {

  "FieldNameExtractor" should "get the (non)optional fields from a case class" in {
    case class A(i: Int, j: Option[Int], k: String)

    val fne = FieldNameExtractor[A]
    fne.extract(nonOptional) shouldBe List("i", "k")
    fne.extract(optional) shouldBe List("j")
  }
}
