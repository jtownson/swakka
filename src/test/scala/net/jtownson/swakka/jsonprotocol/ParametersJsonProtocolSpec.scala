package net.jtownson.swakka.jsonprotocol

import net.jtownson.swakka.jsonprotocol.ParametersJsonProtocol._
import net.jtownson.swakka.jsonschema.SchemaWriter._
import net.jtownson.swakka.model.Parameters.{BodyParameter, HeaderParameter, PathParameter, QueryParameter}
import org.scalatest.Matchers._
import org.scalatest._
import shapeless.{::, HNil}
import spray.json.{JsArray, JsBoolean, JsObject, JsString, _}

class ParametersJsonProtocolSpec extends FlatSpec {

  "ParametersJsonProtocol" should "serialize an array of query params" in {

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
        "required" -> JsBoolean(false),
        "type" -> JsString("integer"),
        "format" -> JsString("int32")
      ),
      JsObject(
        "name" -> JsString("s"),
        "in" -> JsString("query"),
        "required" -> JsBoolean(false),
        "type" -> JsString("string")
      ),
      JsObject(
        "name" -> JsString("t"),
        "in" -> JsString("query"),
        "required" -> JsBoolean(false),
        "type" -> JsString("integer"),
        "format" -> JsString("int32")
      ),
      JsObject(
        "name" -> JsString("u"),
        "in" -> JsString("query"),
        "required" -> JsBoolean(false),
        "type" -> JsString("string")
      ))


    params.toJson shouldBe expectedJson
  }

  it should "serialize header parameters" in {
    type Param = HeaderParameter[String] :: HNil

    val headers = HeaderParameter[String](Symbol("x-my-header"), Some("a header"), false) :: HNil

    val expectedJson = JsArray(
      JsObject(
        "name" -> JsString("x-my-header"),
        "in" -> JsString("header"),
        "description" -> JsString("a header"),
        "required" -> JsBoolean(false),
        "type" -> JsString("string")
      )
    )

    headers.toJson shouldBe expectedJson
  }

  it should "serialize a path parameter" in {

    type Params = PathParameter[String] :: HNil
    val params = PathParameter[String]('petId) :: HNil

    val expectedJson = JsArray(
      JsObject(
        "name" -> JsString("petId"),
        "in" -> JsString("path"),
        "required" -> JsFalse,
        "type" -> JsString("string")
      )
    )

    params.toJson shouldBe expectedJson
  }

  case class Pet(petName: String)
  implicit val petSchemaWriter = schemaWriter(Pet)

  it should "serialize a body param" in {

    type Params = BodyParameter[Pet] :: HNil
    val params = BodyParameter[Pet]('pet) :: HNil

    val expectedJson = JsArray(
      JsObject(
        "name" -> JsString("pet"),
        "in" -> JsString("body"),
        "description" -> JsString(""),
        "required" -> JsFalse,
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

    params.toJson shouldBe expectedJson
  }

  it should "implicitly serialize hnil" in {

    type Params = HNil

    val params: Params = HNil

    val expectedJson = JsArray()

    params.toJson shouldBe expectedJson
  }
}
