package net.jtownson.swakka

import org.scalatest.FlatSpec
import spray.json.{
  JsArray,
  JsBoolean,
  JsNumber,
  JsObject,
  JsString,
  JsValue,
  JsonParser
}

class Temp extends FlatSpec {

  ignore should "be easy to serialize stuff" in {

    val s = JsonParser(
      """
        |{
        | "put": {
        |        "tags": [
        |          "user"
        |        ],
        |        "summary": "Updated user",
        |        "description": "This can only be done by the logged in user.",
        |        "operationId": "updateUser",
        |        "produces": [
        |          "application/xml",
        |          "application/json"
        |        ],
        |        "parameters": [
        |          {
        |            "name": "username",
        |            "in": "path",
        |            "description": "name that need to be updated",
        |            "required": true,
        |            "type": "string"
        |          },
        |          {
        |            "in": "body",
        |            "name": "body",
        |            "description": "Updated user object",
        |            "required": true,
        |            "schema": {
        |              "$ref": "#/definitions/User"
        |            }
        |          }
        |        ],
        |        "responses": {
        |          "400": {
        |            "description": "Invalid user supplied"
        |          },
        |          "404": {
        |            "description": "User not found"
        |          }
        |        }
        |      },
        |      "delete": {
        |        "tags": [
        |          "user"
        |        ],
        |        "summary": "Delete user",
        |        "description": "This can only be done by the logged in user.",
        |        "operationId": "deleteUser",
        |        "produces": [
        |          "application/xml",
        |          "application/json"
        |        ],
        |        "parameters": [
        |          {
        |            "name": "username",
        |            "in": "path",
        |            "description": "The name that needs to be deleted",
        |            "required": true,
        |            "type": "string"
        |          }
        |        ],
        |        "responses": {
        |          "400": {
        |            "description": "Invalid username supplied"
        |          },
        |          "404": {
        |            "description": "User not found"
        |          }
        |        }
        |      }
        |    }
 """.stripMargin
    )

    println(s.toString(printer))
  }

  def printer(jsValue: JsValue): String = jsValue match {
    case JsObject(fields) => {
      val fieldTree = fields
        .map({ case (s, jsv) => s""""$s" -> ${printer(jsv)}""" })
        .mkString(",")
      s"""
         |JsObject(
         |  $fieldTree
         |)
      """.stripMargin
    }

    case JsArray(elements) =>
      s"""JsArray(${elements.map(jsv => s"${printer(jsv)}").mkString(",")})"""

    case JsString(s) => s"""JsString("$s")"""

    case JsBoolean(b) => s"JsBoolean($b)"

    case JsNumber(n) => s"JsNumber($n)"
  }

  val x = JsObject(
    "type" -> JsString("object"),
    "properties" ->
      JsObject(
        "shipDate" ->
          JsObject(
            "type" -> JsString("string"),
            "format" -> JsString("date-time")
          ),
        "quantity" ->
          JsObject(
            "type" -> JsString("integer"),
            "format" -> JsString("int32")
          ),
        "petId" ->
          JsObject(
            "type" -> JsString("integer"),
            "format" -> JsString("int64")
          ),
        "id" ->
          JsObject(
            "type" -> JsString("integer"),
            "format" -> JsString("int64")
          ),
        "complete" ->
          JsObject(
            "type" -> JsString("boolean"),
            "default" -> JsBoolean(false)
          ),
        "status" ->
          JsObject(
            "type" -> JsString("string"),
            "description" -> JsString("Order Status"),
            "enum" -> JsArray(JsString("placed"),
                              JsString("approved"),
                              JsString("delivered"))
          )
      )
  )

}
