package net.jtownson.swakka.jsonprotocol

import net.jtownson.swakka.jsonprotocol.ParametersJsonProtocol._
import net.jtownson.swakka.jsonschema.SchemaWriter._
import net.jtownson.swakka.model.Parameters._
import org.scalatest.Matchers._
import org.scalatest._
import shapeless.{::, HNil}
import spray.json.{JsArray, JsBoolean, JsObject, JsString, _}

class ParametersJsonProtocolSpec extends FlatSpec {

  // TODO: is there a way of generating table driven tests over type params??

  "ParametersJsonProtocol" should "serialize required query parameters" in {

    val params = QueryParameter[String]('qp, Some("a description")) :: HNil

    params.toJson shouldBe queryParamJson(true)
  }

  it should "serialize optional query parameters" in {

    val params = QueryParameter[Option[String]]('qp, Some("a description")) :: HNil

    params.toJson shouldBe queryParamJson(false)
  }

  private def queryParamJson(required: Boolean) = {
    val expectedJson = JsArray(
      JsObject(
        "name" -> JsString("qp"),
        "in" -> JsString("query"),
        "description" -> JsString("a description"),
        "required" -> JsBoolean(required),
        "type" -> JsString("string")
      )
    )
    expectedJson
  }

  it should "serialize required header parameters" in {

    val headers = HeaderParameter[String](Symbol("x-my-header"), Some("a header")) :: HNil

    headers.toJson shouldBe headerParamJson(true)
  }

  it should "serialize optional header parameters" in {

    val headers = HeaderParameter[Option[String]](Symbol("x-my-header"), Some("a header")) :: HNil

    headers.toJson shouldBe headerParamJson(false)
  }

  private def headerParamJson(required: Boolean) = {
    val expectedJson = JsArray(
      JsObject(
        "name" -> JsString("x-my-header"),
        "in" -> JsString("header"),
        "description" -> JsString("a header"),
        "required" -> JsBoolean(required),
        "type" -> JsString("string")
      )
    )
    expectedJson
  }

  it should "serialize required path parameters" in {

    val params = PathParameter[String]('petId) :: HNil

    params.toJson shouldBe pathParameterJson(true)
  }

  it should "serialize optional path parameters" in {

    val params = PathParameter[Option[String]]('petId) :: HNil

    params.toJson shouldBe pathParameterJson(false)
  }

  private def pathParameterJson(required: Boolean) = {
    val expectedJson = JsArray(
      JsObject(
        "name" -> JsString("petId"),
        "in" -> JsString("path"),
        "required" -> JsBoolean(required),
        "type" -> JsString("string")
      )
    )
    expectedJson
  }

  case class Pet(petName: String)

  implicit val petSchemaWriter = schemaWriter(Pet)

  it should "serialize required body parameters" in {

    val params = BodyParameter[Pet]('pet, Some("a description")) :: HNil

    params.toJson shouldBe bodyParameterJson(true)
  }

  it should "serialize optional body parameters" in {

    val params = BodyParameter[Option[Pet]]('pet, Some("a description")) :: HNil

    params.toJson shouldBe bodyParameterJson(false)
  }

  private def bodyParameterJson(required: Boolean) = {
    val expectedJson = JsArray(
      JsObject(
        "name" -> JsString("pet"),
        "in" -> JsString("body"),
        "description" -> JsString("a description"),
        "required" -> JsBoolean(required),
        "schema" -> JsObject(
          "type" -> JsString("object"),
          "required" -> JsArray(JsString("petName")),
          "properties" -> JsObject(
            "petName" -> JsObject(
              "type" -> JsString("string")
            )
          )
        )
      )
    )
    expectedJson
  }

  it should "implicitly serialize hnil" in {

    type Params = HNil

    val params: Params = HNil

    val expectedJson = JsArray()

    params.toJson shouldBe expectedJson
  }

  it should "serialize an hlist of query params" in {

    type Params =
      QueryParameter[Int] :: QueryParameter[String] ::
        QueryParameter[Int] :: QueryParameter[String] :: HNil

    val params =
      QueryParameter[Int]('r) :: QueryParameter[String]('s) ::
        QueryParameter[Int]('t) :: QueryParameter[String]('u) :: HNil

    val expectedJson = JsArray(
      JsObject(
        "name" -> JsString("r"),
        "in" -> JsString("query"),
        "required" -> JsBoolean(true),
        "type" -> JsString("integer"),
        "format" -> JsString("int32")
      ),
      JsObject(
        "name" -> JsString("s"),
        "in" -> JsString("query"),
        "required" -> JsBoolean(true),
        "type" -> JsString("string")
      ),
      JsObject(
        "name" -> JsString("t"),
        "in" -> JsString("query"),
        "required" -> JsBoolean(true),
        "type" -> JsString("integer"),
        "format" -> JsString("int32")
      ),
      JsObject(
        "name" -> JsString("u"),
        "in" -> JsString("query"),
        "required" -> JsBoolean(true),
        "type" -> JsString("string")
      ))

    params.toJson shouldBe expectedJson
  }

  it should "serialize a form parameters" in {

    val params = FormParameter[String, Pet](name = 'f, description = Some("description text"), construct = Pet)

    val expectedJson = JsArray(
      JsObject(
        "name" -> JsString("petName"),
        "in" -> JsString("formData"),
        "required" -> JsBoolean(true),
        "type" -> JsString("string")
      )
    )

    params.toJson shouldBe expectedJson
  }
}
