package net.jtownson.minimal

import net.jtownson.minimal.FlattenJsArray.flatten
import org.scalatest.FlatSpec
import org.scalatest.Matchers._
import spray.json.{JsArray, JsObject, JsString}
import org.scalatest.prop.TableDrivenPropertyChecks._

class FlattenJsArraySpec extends FlatSpec {

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
    "flatten" should s"work for case $name" in {
      flatten(input) shouldBe expectedOutput
    }
  }

}
