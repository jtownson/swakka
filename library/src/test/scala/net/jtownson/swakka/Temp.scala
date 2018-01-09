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

  it should "be easy to ser" in {

    val s = JsonParser(
      """
{
 |      "get": {
 |        "tags": [
 |          "user"
 |        ],
 |        "summary": "Logs out current logged in user session",
 |        "description": "",
 |        "operationId": "logoutUser",
 |        "produces": [
 |          "application/xml",
 |          "application/json"
 |        ],
 |        "parameters": [],
 |        "responses": {
 |          "default": {
 |            "description": "successful operation"
 |          }
 |        }
 |      }
 |    }                  """.stripMargin
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
