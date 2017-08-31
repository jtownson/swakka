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

package net.jtownson.swakka.misc

import net.jtownson.swakka.misc.FieldnameExtractor.{fieldNameTypes, fieldNames}
import org.scalatest.Matchers._
import org.scalatest._

class FieldnameExtractorSpec extends FlatSpec {

  case class A(id: Int, value: Option[String])
  case class B(id: Int, a: A)
  case class X()

  "FieldnameExtractor" should "get the fieldnames of a case class" in {
    fieldNames[A] shouldBe List("id", "value")
    fieldNames[B] shouldBe List("id", "a")
    fieldNames[X] shouldBe Nil
  }

  "FieldnameExtractor" should "get the fieldnames and types of a case class" in {
    fieldNameTypes[A] shouldBe List(("id", "Int"), ("value", "Option"))
    fieldNameTypes[B] shouldBe List(("id", "Int"), ("a", "A"))
    fieldNameTypes[X] shouldBe Nil
  }

}
