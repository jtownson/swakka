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

package net.jtownson.swakka.jsonprotocol

import net.jtownson.swakka.jsonprotocol.Flattener.{flattenToArray, flattenToObject}
import org.scalatest.FlatSpec
import org.scalatest.Matchers._
import org.scalatest.prop.TableDrivenPropertyChecks._
import spray.json.{JsArray, JsObject, JsString}

class FlattenerSpec extends FlatSpec {

  val ai = JsArray(
    JsObject(
      "name" -> JsString("r")),
    JsArray(
      JsObject("name" -> JsString("s")),
      JsObject("name" -> JsString("t")))
  )
  val ao = JsArray(
    JsObject("name" -> JsString("r")),
    JsObject("name" -> JsString("s")),
    JsObject("name" -> JsString("t")))

  val bi = JsArray(
    JsObject(
      "name" -> JsString("r")),
    JsArray(
      JsObject("name" -> JsString("s")),
      JsObject("name" -> JsString("t")),
      JsObject("name" -> JsString("u")))
  )

  val bo = JsArray(
    JsObject("name" -> JsString("r")),
    JsObject("name" -> JsString("s")),
    JsObject("name" -> JsString("t")),
    JsObject("name" -> JsString("u")))

  val ci = JsArray(
    JsObject("name" -> JsString("r")),
    JsObject("name" -> JsString("s")),
    JsObject("name" -> JsString("t"))
  )

  val co = ci

  val di = JsArray(
    JsObject("name" -> JsString("r")),
    JsArray()
  )

  val `do` = JsArray(
    JsObject("name" -> JsString("r")))


  val samples = Table(
    ("name", "input", "expected output"),
    ("a", ai, ao),
    ("b", bi, bo),
    ("c", ci, co),
    ("d", di, `do`)
  )

  forAll(samples) { (name, input, expectedOutput) =>
    "flattenToArray" should s"work for case $name" in {
      flattenToArray(input) shouldBe expectedOutput
    }
  }

  val nested = JsArray(
    JsObject(
      "200" -> JsObject(
        "schema" -> JsObject(
          "type" -> JsString("string")
        )
      )
    ),
    JsArray(
      JsObject(
        "404" -> JsObject(
          "schema" -> JsObject(
            "type" -> JsString("string")
          )
        )
      ),
      JsObject(
        "500" -> JsObject(
          "schema" -> JsObject(
            "type" -> JsString("string")
          )
        )
      )
    )
  )

  val flattened = JsObject(
    "200" -> JsObject(
      "schema" -> JsObject(
        "type" -> JsString("string")
      )
    ),
    "404" -> JsObject(
      "schema" -> JsObject(
        "type" -> JsString("string")
      )
    ),
    "500" -> JsObject(
      "schema" -> JsObject(
        "type" -> JsString("string")
      )
    )
  )


  "flattenToObject" should "work" in {
    flattenToObject(nested) shouldBe flattened
  }

  val duplicated = JsArray(
    JsObject(
      "get" -> JsObject(
        "field" -> JsString("f")
      )),
    JsObject(
      "post" -> JsObject(
        "field" -> JsString("g")
      )
    ))

  val combined = JsObject(
    "200" -> JsObject(
      "schema" -> JsObject(
        "type" -> JsString("string")
      )
    ),
    "404" -> JsObject(
      "schema" -> JsObject(
        "type" -> JsString("string")
      )
    ),
    "500" -> JsObject(
      "schema" -> JsObject(
        "type" -> JsString("string")
      )
    )
  )

}
