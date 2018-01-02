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
        |  "/store/order/{orderId}": {
        |    "get": {
        |      "tags": [
        |        "store"
        |      ],
        |      "summary": "Find purchase order by ID",
        |      "description": "For valid response try integer IDs with value >= 1 and <= 10. Other values will generated exceptions",
        |      "operationId": "getOrderById",
        |      "produces": [
        |        "application/xml",
        |        "application/json"
        |      ],
        |      "parameters": [
        |        {
        |          "name": "orderId",
        |          "in": "path",
        |          "description": "ID of pet that needs to be fetched",
        |          "required": true,
        |          "type": "integer",
        |          "maximum": 10.0,
        |          "minimum": 1.0,
        |          "format": "int64"
        |        }
        |      ],
        |      "responses": {
        |        "200": {
        |          "description": "successful operation",
        |          "schema": {
        |            "$ref": "#/definitions/Order"
        |          }
        |        },
        |        "400": {
        |          "description": "Invalid ID supplied"
        |        },
        |        "404": {
        |          "description": "Order not found"
        |        }
        |      }
        |    }
        |  }
        |}""".stripMargin
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
