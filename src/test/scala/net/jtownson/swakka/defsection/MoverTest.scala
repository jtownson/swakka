package net.jtownson.swakka.defsection

import net.jtownson.swakka.defsection.Mover.move
import org.scalatest.FlatSpec
import org.scalatest.Matchers._
import spray.json._

class MoverTest extends FlatSpec {

  val json ="""
 |{
 |  "paths": {
 |    "/pets": {
 |      "get": {
 |        "tags": ["pets"],
 |        "operationId": "listPets",
 |        "parameters": [{
 |          "format": "int32",
 |          "name": "limit",
 |          "in": "query",
 |          "description": "How many items to return at one time (max 100)",
 |          "type": "integer",
 |          "required": true
 |        }],
 |        "summary": "List all pets",
 |        "responses": {
 |          "200": {
 |            "schema": {
 |              "type": "array",
 |              "items": {
 |                "type": "object",
 |                "properties": {
 |                  "id": {
 |                    "type": "integer",
 |                    "format": "int64"
 |                  },
 |                  "name": {
 |                    "type": "string"
 |                  },
 |                  "tag": {
 |                    "type": "string"
 |                  }
 |                }
 |              }
 |            }
 |          }
 |        }
 |      }
 |    }
 |  }
 |}""".stripMargin

  val expectedJson ="""{
 |  "paths": {
 |    "/pets": {
 |      "get": {
 |        "tags": ["pets"],
 |        "operationId": "listPets",
 |        "parameters": [{
 |          "format": "int32",
 |          "name": "limit",
 |          "in": "query",
 |          "description": "How many items to return at one time (max 100)",
 |          "type": "integer",
 |          "required": true
 |        }],
 |        "summary": "List all pets",
 |        "responses": {
 |          "200": {
 |            "schema": {
 |              "type": "array",
 |              "items": {
 |                "type": "object",
 |                "properties": {
 |                  "id": {
 |                    "type": "integer",
 |                    "format": "int64"
 |                  },
 |                  "name": {
 |                    "type": "string"
 |                  },
 |                  "tag": {
 |                    "type": "string"
 |                  }
 |                }
 |              }
 |            }
 |          }
 |        }
 |      }
 |    }
 |  },
 |  "definitions": {
 |      "Pet": {
 |      "required": [
 |        "id",
 |        "name"
 |      ],
 |      "properties": {
 |        "id": {
 |          "type": "integer",
 |          "format": "int64"
 |        },
 |        "name": {
 |          "type": "string"
 |        },
 |        "tag": {
 |          "type": "string"
 |        }
 |      }
 |    },
 |    "Pets": {
 |      "type": "array",
 |      "items": {
 |        "$ref": "#/definitions/Pet"
 |      }
 |    }
 |  }
 |}""".stripMargin

  "mover" should "create the defn section" in {
    val inputJson = JsonParser(json)
    val expected = JsonParser(expectedJson)
    //move(inputJson) shouldBe expected
  }
}
