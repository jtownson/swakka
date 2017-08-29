package net.jtownson.swakka.jsonprotocol

import net.jtownson.swakka.jsonprotocol.Flattener.{flattenToArray, flattenToObject}
import org.scalatest.FlatSpec
import org.scalatest.Matchers._
import org.scalatest.prop.TableDrivenPropertyChecks._
import spray.json.{JsArray, JsBoolean, JsObject, JsString}

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

  val ei = JsArray(
    JsArray(
      JsObject(
        "format" -> JsString("int32"),
        "name" -> JsString("id"),
        "in" -> JsString("formData"),
        "type" -> JsString("integer"),
        "required" -> JsBoolean(true)),
      JsObject(
        "name" -> JsString("petName"),
        "type" -> JsString("string"),
        "in" -> JsString("formData"),
        "required" -> JsBoolean(true)),
      JsObject(
        "format" -> JsString("float"),
        "name" -> JsString("weight"),
        "in" -> JsString("formData"),
        "type" -> JsString("number"),
        "required" -> JsBoolean(true)
      )),
    JsArray())

  val eo = JsArray(
    JsObject(
      "name" -> JsString("id"),
      "in" -> JsString("formData"),
      "required" -> JsBoolean(true),
      "type" -> JsString("integer"),
      "format" -> JsString("int32")
    ),
    JsObject(
      "name" -> JsString("petName"),
      "in" -> JsString("formData"),
      "required" -> JsBoolean(true),
      "type" -> JsString("string")
    ),
    JsObject(
      "name" -> JsString("weight"),
      "in" -> JsString("formData"),
      "required" -> JsBoolean(true),
      "type" -> JsString("number"),
      "format" -> JsString("float")
    )
  )

  val samples = Table(
    ("name", "input", "expected output"),
    ("a", ai, ao),
    ("b", bi, bo),
    ("c", ci, co),
    ("d", di, `do`),
    ("e", ei, eo)
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
