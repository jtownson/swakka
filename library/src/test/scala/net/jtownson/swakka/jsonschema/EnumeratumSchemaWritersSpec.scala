package net.jtownson.swakka.jsonschema

import enumeratum.{Enum, EnumEntry}

import org.scalatest.FlatSpec
import org.scalatest.Matchers._
import spray.json._

import JsonSchemaJsonProtocol._

class EnumeratumSchemaWritersSpec extends FlatSpec {

  sealed trait StatusEntry extends EnumEntry

  implicit object Status extends Enum[StatusEntry] {
    val values = findValues
    case object good extends StatusEntry
    case object bad extends StatusEntry
    case object ugly extends StatusEntry
  }

  val expectedStatusJson = JsObject(
    "type" -> JsString("string"),
    "description" -> JsString("descriptive string"),
    "enum" -> JsArray(
      JsString("good"),
      JsString("bad"),
      JsString("ugly")
    )
  )

  "EnumeratumSchemaWriter" should "write enum Status implicitly" in {
    JsonSchema[Enum[StatusEntry]](Some("descriptive string")).toJson shouldBe expectedStatusJson
  }

  it should "write enum Status explicitly" in {
    enumeratumSchemaWriter(Status).write(JsonSchema[Enum[StatusEntry]](
      Some("descriptive string"))) shouldBe expectedStatusJson
  }
}
