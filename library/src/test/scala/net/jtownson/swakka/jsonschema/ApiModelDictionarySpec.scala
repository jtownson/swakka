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
import net.jtownson.swakka.jsonschema.ApiModelDictionary._
import org.scalatest.FlatSpec
import org.scalatest.Matchers._

class ApiModelDictionarySpec extends FlatSpec {

  case class A(
                @ApiModelProperty(name = "the name", value = "the value", required = true) foo: Int,
                bar: String,
                baz: Option[Float])

  val dictionary = apiModelDictionary[A]

  "ApiModelDictionary" should "extract ApiModelProperty annotations from a case class" in {
    dictionary("foo") shouldBe ApiModelPropertyEntry(Some("the name"), Some("the value"), true)
  }

  it should "default required to true for non-optional fields" in {
    dictionary("bar") shouldBe ApiModelPropertyEntry(None, None, true)
  }

  it should "default required to false for optional fields" in {
    dictionary("baz") shouldBe ApiModelPropertyEntry(None, None, false)
  }

  it should "maintain field ordering" in {
    dictionary.keys.toList shouldBe List("foo", "bar", "baz")
  }

  "ApiModelKeys" should "maintain field ordering" in {
    apiModelKeys[A] shouldBe Seq("foo", "bar", "baz")
  }
}
