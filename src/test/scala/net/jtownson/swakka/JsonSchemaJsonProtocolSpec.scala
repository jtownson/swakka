package net.jtownson.swakka

import spray.json._
import net.jtownson.swakka.JsonSchemaJsonProtocol._
import org.scalatest.FlatSpec
import org.scalatest.Matchers._
import spray.json.{JsObject, JsString}
import SchemaWriter._

class JsonSchemaJsonProtocolSpec extends FlatSpec {

  "Protocol" should "describe Unit" in {
    JsonSchema[Unit]().toJson shouldBe JsObject()
  }

  it should "describe a String" in {
    JsonSchema[String]().toJson shouldBe JsObject("type" -> JsString("string"))
  }

  it should "describe an Int" in {
    JsonSchema[Int]().toJson shouldBe JsObject("type" -> JsString("number"))
  }

  it should "describe an Double" in {
    JsonSchema[Double]().toJson shouldBe JsObject("type" -> JsString("number"))
  }

  case class A()
  implicit val aWriter = schemaWriter(A)

  it should "describe an empty case class" in {
    JsonSchema[A]().toJson shouldBe JsObject(
      "type" -> JsString("object"),
      "properties" -> JsObject())
  }

  case class B(i: Int)
  implicit val bWriter = schemaWriter(B)

  it should "describe a single field case class" in {
    JsonSchema[B]().toJson shouldBe JsObject(
      "type" -> JsString("object"),
      "properties" -> JsObject(
        "i" -> JsObject(
          "type" -> JsString("number"))
      ))
  }

  case class C(a: A)
  implicit val cWriter = schemaWriter(C)

  it should "describe a nested case class" in {

    JsonSchema[C]().toJson shouldBe JsObject(
      "type" -> JsString("object"),
      "properties" -> JsObject(
        "a" -> JsObject(
          "type" -> JsString("object"),
          "properties" -> JsObject()
        )
      ))
  }

  case class D(id: Int, value: String)
  implicit val dWriter = schemaWriter(D)

  it should "describe a two field case class" in {
    JsonSchema[D]().toJson shouldBe JsObject(
      "type" -> JsString("object"),
      "properties" -> JsObject(
        "id" -> JsObject(
          "type" -> JsString("number")),
        "value" -> JsObject(
          "type" -> JsString("string")
        )
      ))
  }

}
