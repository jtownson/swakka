/*
 * Copyright 2017 Jeremy Townson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.jtownson.swakka.jsonschema

import akka.http.scaladsl.model.DateTime
import io.swagger.annotations.ApiModelProperty
import net.jtownson.swakka.jsonschema.JsonSchemaJsonProtocol._
import org.scalatest.FlatSpec
import org.scalatest.Matchers._
import spray.json.{JsObject, JsString, _}

class SchemaWriterSpec extends FlatSpec {

  "Protocol" should "describe Unit" in {
    JsonSchema[Unit]().toJson shouldBe JsObject()
  }

  it should "describe a String" in {
    JsonSchema[String]().toJson shouldBe JsObject("type" -> JsString("string"))
  }

  it should "describe a Boolean" in {
    JsonSchema[Boolean]().toJson shouldBe JsObject("type" -> JsString("boolean"))
  }

  it should "describe an Int" in {
    JsonSchema[Int]().toJson shouldBe JsObject("type" -> JsString("integer"), "format" -> JsString("int32"))
  }

  it should "describe a Long" in {
    JsonSchema[Long]().toJson shouldBe JsObject("type" -> JsString("integer"), "format" -> JsString("int64"))
  }

  it should "describe an Float" in {
    JsonSchema[Float]().toJson shouldBe JsObject("type" -> JsString("number"), "format" -> JsString("float"))
  }

  it should "describe an Double" in {
    JsonSchema[Double]().toJson shouldBe JsObject("type" -> JsString("number"), "format" -> JsString("double"))
  }

  case class A()

  it should "describe an empty case class" in {
    JsonSchema[A]().toJson shouldBe JsObject(
      "type" -> JsString("object"),
      "properties" -> JsObject())
  }

  case class B(i: Int)

  it should "describe a single field case class" in {
    JsonSchema[B]().toJson shouldBe JsObject(
      "type" -> JsString("object"),
      "required" -> JsArray(JsString("i")),
      "properties" -> JsObject(
        "i" -> JsObject(
          "type" -> JsString("integer"),
          "format" -> JsString("int32"))
      ))
  }

  case class C(a: A)

  it should "describe a nested case class" in {

    JsonSchema[C]().toJson shouldBe JsObject(
      "type" -> JsString("object"),
      "required" -> JsArray(JsString("a")),
      "properties" -> JsObject(
        "a" -> JsObject(
          "type" -> JsString("object"),
          "properties" -> JsObject()
        )
      ))
  }

  case class D(id: Int, value: String)

  it should "describe a two field case class" in {
    JsonSchema[D]().toJson shouldBe JsObject(
      "type" -> JsString("object"),
      "required" -> JsArray(JsString("id"), JsString("value")),
      "properties" -> JsObject(
        "id" -> JsObject(
          "type" -> JsString("integer"),
          "format" -> JsString("int32")),
        "value" -> JsObject(
          "type" -> JsString("string")
        )
      ))
  }

  case class E(
                @ApiModelProperty("the id") id: Int,
                @ApiModelProperty("the value") value: String
              )

  it should "produce annotated docs" in {

    JsonSchema[E](Some("the parent")).toJson shouldBe JsObject(
      "type" -> JsString("object"),
      "description" -> JsString("the parent"),
      "required" -> JsArray(JsString("id"), JsString("value")),
      "properties" -> JsObject(
        "id" -> JsObject(
          "description" -> JsString("the id"),
          "type" -> JsString("integer"),
          "format" -> JsString("int32")),
        "value" -> JsObject(
          "description" -> JsString("the value"),
          "type" -> JsString("string")
        )
      ))
  }

  case class F(@ApiModelProperty("the nested e") e: E)


  it should "produce annotated docs for nested classes" in {

    JsonSchema[F](Some("the parent F")).toJson shouldBe JsObject(
      "type" -> JsString("object"),
      "description" -> JsString("the parent F"),
      "required" -> JsArray(JsString("e")),
      "properties" -> JsObject(
        "e" -> JsObject(
          "type" -> JsString("object"),
          "description" -> JsString("the nested e"),
          "required" -> JsArray(JsString("id"), JsString("value")),
          "properties" -> JsObject(
            "id" -> JsObject(
              "description" -> JsString("the id"),
              "type" -> JsString("integer"),
              "format" -> JsString("int32")),
            "value" -> JsObject(
              "description" -> JsString("the value"),
              "type" -> JsString("string")
            )
          ))
      ))
  }

  case class G(o: Option[String])


  it should "produce a schema for options" in {
    JsonSchema[G]().toJson shouldBe JsObject(
      "type" -> JsString("object"),
      "properties" -> JsObject(
        "o" -> JsObject(
          "type" -> JsString("string"))
      )
    )
  }

  case class H(s: Seq[String])


  it should "produce a schema for Seq" in {
    JsonSchema[H]().toJson shouldBe JsObject(
      "type" -> JsString("object"),
      "required" -> JsArray(JsString("s")),
      "properties" -> JsObject(
        "s" -> JsObject(
          "type" -> JsString("array"),
          "items" -> JsObject(
            "type" -> JsString("string")
          )
        )
      )
    )
  }

  object OrderStatus extends Enumeration {
    type OrderStatus = Value
    val placed, approved, delivered = Value
  }
  implicit val implicitOrderStatus = OrderStatus
  import OrderStatus._
  case class Order(id: Long,
                   status: OrderStatus)

  it should "describe a case class containing an Enum" in {

    JsonSchema[Order]().toJson shouldBe JsObject(
      "type" -> JsString("object"),
      "required" -> JsArray(JsString("id"), JsString("status")),
      "properties" -> JsObject(
        "id" -> JsObject(
          "type" -> JsString("integer"),
          "format" -> JsString("int64")),
        "status" -> JsObject(
          "type" -> JsString("string"),
          "enum" -> JsArray(
            JsString("placed"),
            JsString("approved"),
            JsString("delivered")
          )
        )
      ))
  }

  it should "describe and akka datetime" in {
    JsonSchema[DateTime]().toJson shouldBe JsObject(
      "type" -> JsString("§string"),
      "format" -> JsString("date-time")
    )
  }
}
