package net.jtownson.swakka

import net.jtownson.swakka.OpenApiModel.{BodyParameter, PathParameter, QueryParameter}
import net.jtownson.swakka.ParametersJsonProtocol._
import net.jtownson.swakka.jsonschema.SchemaWriter._
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
        "description" -> JsString(""),
        "required" -> JsBoolean(false),
        "type" -> JsString("integer")
      ),
      JsObject(
        "name" -> JsString("s"),
        "in" -> JsString("query"),
        "description" -> JsString(""),
        "required" -> JsBoolean(false),
        "type" -> JsString("string")
      ),
      JsObject(
        "name" -> JsString("t"),
        "in" -> JsString("query"),
        "description" -> JsString(""),
        "required" -> JsBoolean(false),
        "type" -> JsString("integer")
      ),
      JsObject(
        "name" -> JsString("u"),
        "in" -> JsString("query"),
        "description" -> JsString(""),
        "required" -> JsBoolean(false),
        "type" -> JsString("string")
      ))


    params.toJson shouldBe expectedJson
  }


  it should "serialize a path parameter" in {

    type Params = PathParameter[String] :: HNil
    val params = PathParameter[String]('petId) :: HNil

    val expectedJson = JsArray(
      JsObject(
        "name" -> JsString("petId"),
        "in" -> JsString("path"),
        "description" -> JsString(""),
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
}
