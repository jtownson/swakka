package net.jtownson.swakka.jsonschema

import akka.http.scaladsl.model.DateTime
import org.scalatest.FlatSpec
import org.scalatest.Matchers._
import spray.json.{JsObject, JsString}

class DateSchemaWritersTest extends FlatSpec {

  "DateSchemaWriter" should "write a date schema" in {
    DateSchemaWriters.akkaHttpDateSchemaWriter.write(
      JsonSchema[DateTime](Some("description"))) shouldBe
      JsObject(
        "type" -> JsString("string"),
        "format" -> JsString("date-time"),
        "description" -> JsString("description")
      )
  }
}
