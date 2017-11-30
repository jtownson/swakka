package net.jtownson.swakka.jsonschema

import org.scalatest.FlatSpec
import org.scalatest.Matchers._
import spray.json._

import JsonSchemaJsonProtocol._

class EnumSchemaWritersSpec extends FlatSpec {

  implicit object status extends Enumeration {
    val good, bad, ugly = Value
  }

  type Status = status.type

  val expectedStatusJson = JsObject(
    "type" -> JsString("string"),
    "description" -> JsString("descriptive string"),
    "enum" -> JsArray(
      JsString("good"),
      JsString("bad"),
      JsString("ugly")
    )
  )

  "EnumSchemaWriter" should "write enum Status implicitly" in {
    JsonSchema[status.Value](Some("descriptive string")).toJson shouldBe expectedStatusJson
  }

  it should "write enum Status explicitly" in {
    enumSchemaWriter(status).write(JsonSchema[status.Value](
      Some("descriptive string"))) shouldBe expectedStatusJson
  }
}
