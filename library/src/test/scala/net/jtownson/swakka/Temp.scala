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

  ignore should "be easy to ser" in {

    val s = JsonParser(
      """{
        |      "type": "object",
        |      "properties": {
        |        "id": {
        |          "type": "integer",
        |          "format": "int64"
        |        },
        |        "petId": {
        |          "type": "integer",
        |          "format": "int64"
        |        },
        |        "quantity": {
        |          "type": "integer",
        |          "format": "int32"
        |        },
        |        "shipDate": {
        |          "type": "string",
        |          "format": "date-time"
        |        },
        |        "status": {
        |          "type": "string",
        |          "description": "Order Status",
        |          "enum": [
        |            "placed",
        |            "approved",
        |            "delivered"
        |          ]
        |        },
        |        "complete": {
        |          "type": "boolean",
        |          "default": false
        |        }
                |    }}       """.stripMargin
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
