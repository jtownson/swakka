package net.jtownson.jsonschema

import org.scalatest.FlatSpec
import org.scalatest.Matchers._
import org.scalatest.prop.TableDrivenPropertyChecks._
import JsonSchemaTypes._
import net.jtownson.TestResource
import spray.json._
import spray.json.DefaultJsonProtocol._
import JsonFormats._
import scala.collection.immutable.TreeMap

class JsonFormatsSpec extends FlatSpec {

  private val stringDefaultSchema = JsonSchema(
    `type` = Some(JsonString),
    default = Some("foo")
  )

  private val stringDefaultJson = JsObject(
    "type" -> JsString("string"),
    "default" -> JsString("foo")
  )

  case class A(s: String, i: Int)
  implicit val aFormat: RootJsonFormat[A] = jsonFormat2(A)
  private val caseADefaultSchema = JsonSchema(
    `type` = Some(JsonObject),
    default = Some(A("foo", 1))
  )

  private val caseADefaultJson = JsObject(
    "type" -> JsString("object"),
    "default" -> JsObject(
      "s" -> JsString("foo"),
      "i" -> JsNumber(1)
    )
  )

//  private val patternPropsSchema = JsonSchema(
//    patternProperties = Some(Map(
//      "^x-" -> JsonSchema(
//        `$ref` = Some("#/definitions/vendorExtension")))))
//
//  private val patternPropsJson = JsObject(
//    "patternProperties" -> JsObject(
//      "^x-" -> JsObject(
//        "$ref" -> JsString("#/definitions/vendorExtension")
//      )
//    ))
//
//  private val anyOfSchema = JsonSchema(
//    anyOf = Some(List(
//        JsonSchema()
//      )
//    )
//  )
//
//  private val anyOfJson = JsObject(
//    "anyOf" -> JsArray(Vector(
//      JsObject()
//    )
//    )
//  )

  val schemas =
    Table[String, JsonSchema[_], JsObject](
      ("schema-id", "schema", "json"),
      ("empty schema", JsonSchema(), JsObject()),
      ("schema ref", JsonSchema(`$schema` = Some("url")), JsObject("$schema" -> JsString("url"))),
      ("single string", JsonSchema(`type`= Some(JsonString)), JsObject("type" -> JsString("string"))),
      ("single null", JsonSchema(`type`= Some(JsonNull)), JsObject("type" -> JsString("null"))),
      ("single number", JsonSchema(`type`= Some(JsonNumber)), JsObject("type" -> JsString("number"))),
      ("a ref", JsonSchema(`$ref`=Some("#/definitions/definition")), JsObject("$ref" -> JsString("#/definitions/definition"))),
      //("empty pattern props", JsonSchema(patternProperties = Some(Map())), JsObject("patternProperties" -> JsObject())),
      //("pattern props", patternPropsSchema, patternPropsJson),
      //("anyOf", anyOfSchema, anyOfJson),
      ("default strings", stringDefaultSchema, stringDefaultJson),
      ("default case class", caseADefaultSchema, caseADefaultJson)//,
//      ("seq of schemas", Seq(JsonSchema(), JsArray(JsObject()))),
//      ("additional props as boolean", JsonSchema(additionalProperties = Some(Coproduct[BooleanOrSchema[_]](false))), JsObject("additionalProperties" -> JsFalse))
    )

//  forAll(schemas) { (schemaId: String, schema: JsonSchema[_], json: JsObject) =>
//    s"read '$schemaId'" should "work" in {
//      jsonSchemaFormat.read(json) shouldBe schema
//    }
//
//    s"write '$schemaId" should "work" in {
//      jsonSchemaFormat.write(schema) shouldBe json
//    }
//  }

//  "swagger spec" should "be parsed correctly" in {
//    val schemaData: String = TestResource.get("/schema.simple.json").mkString
//
//    val expectedJson: JsValue = JsonParser(schemaData)
//
//    val parsedJsonSchema: JsonSchema[_] = jsonSchemaFormat.read(expectedJson)
//
//    val actualJson: JsValue = jsonSchemaFormat.write(parsedJsonSchema)
//
//    val actualJsonMap = toMap(actualJson.asJsObject)
//    val expectedJsonMap = toMap(expectedJson.asJsObject)
//
//    actualJsonMap shouldBe expectedJsonMap
//  }

  private def toMap(json: JsObject): Map[String, Any] = {

    def toValue(json: JsValue): Any = json match {
      case JsObject(fields: Map[String, JsValue]) => TreeMap(fields.toList: _*).map((kv: (String, JsValue)) => (kv._1, toValue(kv._2)))
      case JsArray(elements) => elements.map(jsValue => toValue(jsValue)).toSet
      case JsString(value) => value
      case JsBoolean(value) => value
      case JsNumber(value) => value
      case JsNull => "null"
    }

    val f = (m: Map[String, Any], kv: (String, JsValue)) => m.+((kv._1, toValue(kv._2)))

    json.fields.foldLeft[Map[String, Any]](new TreeMap())(f)
  }

  private def areEqual(lhs: JsObject, rhs: JsObject): Boolean = toMap(lhs) == toMap(rhs)
}
