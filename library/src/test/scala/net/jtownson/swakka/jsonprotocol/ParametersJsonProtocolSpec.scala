package net.jtownson.swakka.jsonprotocol

import net.jtownson.swakka.jsonprotocol.ParametersJsonProtocol._
import net.jtownson.swakka.jsonschema.SchemaWriter._
import net.jtownson.swakka.misc.jsObject
import net.jtownson.swakka.model.Parameters._
import org.scalatest.Matchers._
import org.scalatest._
import shapeless.{::, HNil}
import spray.json.{JsArray, JsBoolean, JsObject, JsString, _}

class ParametersJsonProtocolSpec extends FlatSpec {

  // TODO: testcases for non-string types
  // TODO: is there a way of generating table driven tests over type params??

  "ParametersJsonProtocol" should "serialize required query parameters" in {

    val params = QueryParameter[String]('qp, Some("a description")) :: HNil

    params.toJson shouldBe queryParamJson(true)
  }

  it should "serialize optional query parameters" in {

    val params = QueryParameter[Option[String]]('qp, Some("a description"), Some(Some("default-value"))) :: HNil

    params.toJson shouldBe queryParamJson(false, Some("default-value"))
  }

  it should "serialize required header parameters" in {

    val headers = HeaderParameter[String](Symbol("x-my-header"), Some("a header")) :: HNil

    headers.toJson shouldBe headerParamJson(true)
  }

  it should "serialize optional header parameters" in {

    val headers = HeaderParameter[Option[String]](Symbol("x-my-header"), Some("a header"), Some(Some("default-value"))) :: HNil

    headers.toJson shouldBe headerParamJson(false, Some("default-value"))
  }

  it should "serialize required path parameters" in {

    val params = PathParameter[String]('petId) :: HNil

    params.toJson shouldBe pathParameterJson(true)
  }

  case class Pet(petName: String)
  implicit val petJsonWriter = jsonFormat1(Pet)
  implicit val petSchemaWriter = schemaWriter(Pet)

  it should "serialize required body parameters" in {

    val params = BodyParameter[Pet]('pet, Some("a description")) :: HNil

    params.toJson shouldBe bodyParameterJson(true)
  }

  it should "serialize optional body parameters" in {
    val defaultPet = Pet("I'm a default. Boo!")
    val params = BodyParameter[Option[Pet]]('pet, Some("a description"), Some(Some(defaultPet))) :: HNil

    params.toJson shouldBe bodyParameterJson(false, Some(defaultPet.toJson))
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

  import net.jtownson.swakka.routegen.Tuplers._
  import FormParameterType._

  it should "serialize single-field, string form params" in {

    implicit val formParamFormat = requiredFormParameterFormat(Pet)

    val params = FormParameter[(String), Pet](
      'f, Some("form description"),
      construct = Pet) :: HNil

    params.toJson shouldBe JsArray(
      JsObject(
        "name" -> JsString("petName"),
        "in"-> JsString("formData"),
        "required" -> JsBoolean(true),
        "type" -> JsString("string")
      )
    )
  }

  case class BigPet(id: Int, petName: String, weight: Float)

  it should "serialize multi-field, form params" in {

    implicit val formParamFormat = requiredFormParameterFormat(BigPet)

    val params = FormParameter[(Int, String, Float), BigPet](
      'f, Some("form description"),
      construct = BigPet) :: HNil

    params.toJson shouldBe JsArray(
      JsObject(
        "name" -> JsString("id"),
        "in"-> JsString("formData"),
        "required" -> JsBoolean(true),
        "type" -> JsString("integer"),
        "format" -> JsString("int32")
      ),
      JsObject(
        "name" -> JsString("petName"),
        "in"-> JsString("formData"),
        "required" -> JsBoolean(true),
        "type" -> JsString("string")
      ),
      JsObject(
        "name" -> JsString("weight"),
        "in"-> JsString("formData"),
        "required" -> JsBoolean(true),
        "type" -> JsString("number"),
        "format" -> JsString("float")
      )
    )
  }

  object Statuses extends Enumeration {
    val placed, approved, delivered = Value
  }

  it should "serialize query params for scala enums without getting involved in the world of scala enums" in {

    import Statuses._
    val params = QueryParameter[Option[String]](
      name = 'status,
      description = Some("order status"),
      default = Some(Some(placed.toString)),
      enum = Some(Statuses.values.toList.map(value => Some(value.toString)))) :: HNil

    val expectedJson = JsArray(
      JsObject(
        "name" -> JsString("status"),
        "in" -> JsString("query"),
        "description" -> JsString("order status"),
        "required" -> JsBoolean(false),
        "type" -> JsString("string"),
        "default" -> JsString("placed"),
        "enum" -> JsArray(JsString("placed"), JsString("approved"), JsString("delivered"))
      )
    )

    params.toJson shouldBe expectedJson
  }

  private def queryParamJson(required: Boolean, default: Option[String] = None) =
    JsArray(
      jsObject(
        Some("name" -> JsString("qp")),
        Some("in" -> JsString("query")),
        Some("description" -> JsString("a description")),
        Some("required" -> JsBoolean(required)),
        Some("type" -> JsString("string")),
        default.map("default" -> JsString(_))
      )
    )

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

  private def bodyParameterJson(required: Boolean, default: Option[JsValue] = None) =
    JsArray(
      jsObject(
        Some("name" -> JsString("pet")),
        Some("in" -> JsString("body")),
        Some("description" -> JsString("a description")),
        Some("required" -> JsBoolean(required)),
        default.map("default" -> _),
        Some("schema" -> JsObject(
          "type" -> JsString("object"),
          "required" -> JsArray(JsString("petName")),
          "properties" -> JsObject(
            "petName" -> JsObject(
              "type" -> JsString("string")
            )
          )
        ))
      )
    )

  private def headerParamJson(required: Boolean, default: Option[String] = None) =
    JsArray(
      jsObject(
        Some("name" -> JsString("x-my-header")),
        Some("in" -> JsString("header")),
        Some("description" -> JsString("a header")),
        Some("required" -> JsBoolean(required)),
        Some("type" -> JsString("string")),
        default.map("default" -> JsString(_))
      )
    )
}
