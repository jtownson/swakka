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

  "json" should "be easy to ser" in {

    val s = JsonParser(
      """{
        |      "delete": {
        |        "tags": [
        |          "pet"
        |        ],
        |        "summary": "Deletes a pet",
        |        "description": "",
        |        "operationId": "deletePet",
        |        "produces": [
        |          "application/xml",
        |          "application/json"
        |        ],
        |        "parameters": [
        |          {
        |            "name": "api_key",
        |            "in": "header",
        |            "required": false,
        |            "type": "string"
        |          },
        |          {
        |            "name": "petId",
        |            "in": "path",
        |            "description": "Pet id to delete",
        |            "required": true,
        |            "type": "integer",
        |            "format": "int64"
        |          }
        |        ],
        |        "responses": {
        |          "400": {
        |            "description": "Invalid ID supplied"
        |          },
        |          "404": {
        |            "description": "Pet not found"
        |          }
        |        },
        |        "security": [
        |          {
        |            "petstore_auth": [
        |              "write:pets",
        |              "read:pets"
        |            ]
        |          }
        |        ]
        |      }
        |    }
        | 
      """.stripMargin
    )

//    println(s.toString(printer))
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
    "delete" ->
      JsObject(
        "security" -> JsArray(
          JsObject(
            "petstore_auth" -> JsArray(JsString("write:pets"),
                                       JsString("read:pets"))
          )
        ),
        "description" -> JsString(""),
        "tags" -> JsArray(JsString("pet")),
        "operationId" -> JsString("deletePet"),
        "produces" -> JsArray(JsString("application/xml"),
                              JsString("application/json")),
        "parameters" -> JsArray(
          JsObject(
            "name" -> JsString("api_key"),
            "in" -> JsString("header"),
            "required" -> JsBoolean(false),
            "type" -> JsString("string")
          ),
          JsObject(
            "format" -> JsString("int64"),
            "name" -> JsString("petId"),
            "in" -> JsString("path"),
            "description" -> JsString("Pet id to delete"),
            "type" -> JsString("integer"),
            "required" -> JsBoolean(true)
          )
        ),
        "summary" -> JsString("Deletes a pet"),
        "responses" ->
          JsObject(
            "400" ->
              JsObject(
                "description" -> JsString("Invalid ID supplied")
              ),
            "404" ->
              JsObject(
                "description" -> JsString("Pet not found")
              )
          )
      )
  )

}
