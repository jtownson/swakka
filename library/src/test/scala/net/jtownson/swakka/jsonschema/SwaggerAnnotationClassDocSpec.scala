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

import io.swagger.annotations.ApiModelProperty

import net.jtownson.swakka.jsonschema.SwaggerAnnotationClassDoc._
import org.scalatest.FlatSpec
import org.scalatest.Matchers._

class SwaggerAnnotationClassDocSpec extends FlatSpec {

  case class A(
                @ApiModelProperty("the value") foo: Int,
                bar: String,
                @ApiModelProperty("the baz") baz: Option[Float])

  val docEntries = ClassDoc.entries[A]

  "ApiModelDictionary" should "extract ApiModelProperty annotations from a case class" in {
    docEntries("foo") shouldBe FieldDoc("the value")
    docEntries("baz") shouldBe FieldDoc("the baz")
  }

  it should "skip undocumented fields" in {
    docEntries contains "bar" shouldBe false
  }

  it should "maintain field ordering" in {
    docEntries.keys.toList shouldBe List("foo", "baz")
  }

  it should "support overridden field docs" in {
    implicit val tDocs: ClassDoc[A] = ClassDoc[A](Map("foo" -> FieldDoc("foo")))

    val overriddenDocEntries = ClassDoc.entries[A]
    overriddenDocEntries("foo") shouldBe FieldDoc("foo")
    overriddenDocEntries.contains("baz") shouldBe false

  }
}
