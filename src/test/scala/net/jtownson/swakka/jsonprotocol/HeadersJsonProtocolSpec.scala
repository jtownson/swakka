package net.jtownson.swakka.jsonprotocol

import net.jtownson.swakka.OpenApiModel.Header
import org.scalatest.FlatSpec
import org.scalatest.Matchers._
import spray.json._
import HeadersJsonProtocol._
import shapeless.{HNil, ::}

class HeadersJsonProtocolSpec extends FlatSpec {

  "HeadersJsonProtocol" should "serialize a string" in {

    val expectedJson = JsObject(
      "x-foo" -> JsObject(
        "type" -> JsString("string"),
        "description" -> JsString("some info")
      ))

    Header[String](Symbol("x-foo"), Some("some info")).toJson shouldBe expectedJson
  }

  it should "serialize a Double" in {

    val expectedJson = JsObject(
      "x-foo" -> JsObject(
        "type" -> JsString("number"),
        "format" -> JsString("double"),
        "description" -> JsString("some info")
      ))

    Header[Double](Symbol("x-foo"), Some("some info")).toJson shouldBe expectedJson
  }

  it should "serialize a Float" in {

    val expectedJson = JsObject(
      "x-foo" -> JsObject(
        "type" -> JsString("number"),
        "format" -> JsString("float"),
        "description" -> JsString("some info")
      ))

    Header[Float](Symbol("x-foo"), Some("some info")).toJson shouldBe expectedJson
  }

  it should "serialize an Int" in {

    val expectedJson = JsObject(
      "x-foo" -> JsObject(
        "type" -> JsString("integer"),
        "format" -> JsString("int32"),
        "description" -> JsString("some info")
      ))

    Header[Int](Symbol("x-foo"), Some("some info")).toJson shouldBe expectedJson
  }

  it should "serialize a Long" in {

    val expectedJson = JsObject(
      "x-foo" -> JsObject(
        "type" -> JsString("integer"),
        "format" -> JsString("int64"),
        "description" -> JsString("some info")
      ))

    Header[Long](Symbol("x-foo"), Some("some info")).toJson shouldBe expectedJson
  }

  it should "serialize a Boolean" in {

    val expectedJson = JsObject(
      "x-foo" -> JsObject(
        "type" -> JsString("boolean"),
        "description" -> JsString("some info")
      ))

    Header[Boolean](Symbol("x-foo"), Some("some info")).toJson shouldBe expectedJson
  }

  it should "serialize a combination of headers" in {

    type Headers = Header[String] :: Header[Int] :: HNil

    val headers = Header[String](Symbol("x-s"), Some("a string header")) :: Header[Int](Symbol("x-i"), Some("an int header")) :: HNil

    val expectedJson = JsObject(
      "x-s" -> JsObject(
        "type" -> JsString("string"),
        "description" -> JsString("a string header")
      ),
      "x-i" -> JsObject(
        "type" -> JsString("integer"),
        "format" -> JsString("int32"),
        "description" -> JsString("an int header")
      )
    )

    headers.toJson shouldBe expectedJson
  }

}
