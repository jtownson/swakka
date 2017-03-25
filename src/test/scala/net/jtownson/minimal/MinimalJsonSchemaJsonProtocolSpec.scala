package net.jtownson.minimal

import net.jtownson.minimal.MinimalJsonSchemaJsonProtocol._
import org.scalatest.FlatSpec
import org.scalatest.Matchers._
import spray.json.{JsObject, JsString}

class MinimalJsonSchemaJsonProtocolSpec extends FlatSpec {

  "Protocol" should "describe Unit" in {
    val jsonFormat = new MinimalJsonSchemaJsonProtocol[Unit].jsonSchemaJsonWriter
    val schema = MinimalJsonSchema[Unit]()

    jsonFormat.write(schema) shouldBe JsObject()
  }

  it should "describe a String" in {
    val jsonFormat = new MinimalJsonSchemaJsonProtocol[String].jsonSchemaJsonWriter
    val schema = MinimalJsonSchema[String]()

    jsonFormat.write(schema) shouldBe JsObject("type" -> JsString("string"))
  }

  it should "describe an Int" in {
    val jsonFormat = new MinimalJsonSchemaJsonProtocol[Int].jsonSchemaJsonWriter
    val schema = MinimalJsonSchema[Int]()

    jsonFormat.write(schema) shouldBe JsObject("type" -> JsString("number"))
  }

  it should "describe an Double" in {
    val jsonFormat = new MinimalJsonSchemaJsonProtocol[Double].jsonSchemaJsonWriter
    val schema = MinimalJsonSchema[Double]()

    jsonFormat.write(schema) shouldBe JsObject("type" -> JsString("number"))
  }

  case class A()

  it should "describe an empty case class" in {
    implicit val aWriter = schemaWriter(A)
    val jsonFormat = new MinimalJsonSchemaJsonProtocol[A].jsonSchemaJsonWriter
    val schema = MinimalJsonSchema[A]()

    jsonFormat.write(schema) shouldBe JsObject(
      "type" -> JsString("object"),
      "properties" -> JsObject())
  }

  case class B(i: Int)

  it should "describe a single field case class" in {
    implicit val bWriter = schemaWriter(B)
    val jsonFormat = new MinimalJsonSchemaJsonProtocol[B].jsonSchemaJsonWriter
    val schema = MinimalJsonSchema[B]()

    jsonFormat.write(schema) shouldBe JsObject(
      "type" -> JsString("object"),
      "properties" -> JsObject(
        "i" -> JsObject(
          "type" -> JsString("number"))
      ))
  }

  case class C(a: A)

  it should "describe a nested case class" in {

    implicit val aWriter = schemaWriter(A)
    implicit val cWriter = schemaWriter(C)
    val jsonFormat = new MinimalJsonSchemaJsonProtocol[C].jsonSchemaJsonWriter
    val schema = MinimalJsonSchema[C]()

    jsonFormat.write(schema) shouldBe JsObject(
      "type" -> JsString("object"),
      "properties" -> JsObject(
        "a" -> JsObject(
          "type" -> JsString("object"),
          "properties" -> JsObject()
        )
      ))
  }

  case class D(id: Int, value: String)

  it should "describe a two field case class" in {
    implicit val aWriter = schemaWriter(D)
    val jsonFormat = new MinimalJsonSchemaJsonProtocol[D].jsonSchemaJsonWriter
    val schema = MinimalJsonSchema[D]()

    jsonFormat.write(schema) shouldBe JsObject(
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
