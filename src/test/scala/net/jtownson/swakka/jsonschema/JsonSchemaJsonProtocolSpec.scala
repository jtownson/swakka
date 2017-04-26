package net.jtownson.swakka.jsonschema

import io.swagger.annotations.ApiModelProperty
import net.jtownson.swakka.jsonschema.JsonSchemaJsonProtocol._
import net.jtownson.swakka.jsonschema.SchemaWriter._
import org.scalatest.FlatSpec
import org.scalatest.Matchers._
import spray.json.{JsObject, JsString, _}

class JsonSchemaJsonProtocolSpec extends FlatSpec {

  "Protocol" should "describe Unit" in {
    JsonSchema[Unit]().toJson shouldBe JsObject()
  }

  it should "describe a String" in {
    JsonSchema[String]().toJson shouldBe JsObject("type" -> JsString("string"))
  }

  it should "describe an Int" in {
    JsonSchema[Int]().toJson shouldBe JsObject("type" -> JsString("integer"), "format" -> JsString("int32"))
  }

  it should "describe a Long" in {
    JsonSchema[Long]().toJson shouldBe JsObject("type" -> JsString("integer"), "format" -> JsString("int64"))
  }

  it should "describe an Float" in {
    JsonSchema[Float]().toJson shouldBe JsObject("type" -> JsString("number"), "format" -> JsString("float"))
  }

  it should "describe an Double" in {
    JsonSchema[Double]().toJson shouldBe JsObject("type" -> JsString("number"), "format" -> JsString("double"))
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
          "type" -> JsString("integer"),
          "format" -> JsString("int32"))
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
          "type" -> JsString("integer"),
          "format" -> JsString("int32")),
        "value" -> JsObject(
          "type" -> JsString("string")
        )
      ))
  }

  case class E(
                @ApiModelProperty(value = "the id") id: Int,
                @ApiModelProperty(value = "the value") value: String
              )

  implicit val eWriter = schemaWriter(E)

  it should "produce annotated docs" in {

    JsonSchema[E](Some("the parent")).toJson shouldBe JsObject(
      "type" -> JsString("object"),
      "description" -> JsString("the parent"),
      "properties" -> JsObject(
        "id" -> JsObject(
          "description" -> JsString("the id"),
          "type" -> JsString("integer"),
          "format" -> JsString("int32")),
        "value" -> JsObject(
          "description" -> JsString("the value"),
          "type" -> JsString("string")
        )
      ))
  }

  case class F(@ApiModelProperty(value = "the nested e") e: E)

  implicit val fWriter = schemaWriter(F)

  it should "produce annotated docs for nested classes" in {

    JsonSchema[F](Some("the parent F")).toJson shouldBe JsObject(
      "type" -> JsString("object"),
      "description" -> JsString("the parent F"),
      "properties" -> JsObject(
        "e" -> JsObject(
          "type" -> JsString("object"),
          "description" -> JsString("the nested e"),
          "properties" -> JsObject(
            "id" -> JsObject(
              "description" -> JsString("the id"),
              "type" -> JsString("integer"),
              "format" -> JsString("int32")),
            "value" -> JsObject(
              "description" -> JsString("the value"),
              "type" -> JsString("string")
            )
          ))
      ))
  }

  case class G(o: Option[String])

  implicit val gWriter = schemaWriter(G)

  it should "produce a schema for options" in {
    JsonSchema[G]().toJson shouldBe JsObject(
      "type" -> JsString("object"),
      "properties" -> JsObject(
        "o" -> JsObject(
          "type" -> JsString("string"))
      )
    )
  }

  case class H(s: Seq[String])

  implicit val hWriter = schemaWriter(H)

  it should "produce a schema for Seq" in {
    JsonSchema[H]().toJson shouldBe JsObject(
      "type" -> JsString("object"),
      "properties" -> JsObject(
        "s" -> JsObject(
          "type" -> JsString("array"),
          "items" -> JsObject(
            "type" -> JsString("string")
          )
        )
      )
    )
  }
}
