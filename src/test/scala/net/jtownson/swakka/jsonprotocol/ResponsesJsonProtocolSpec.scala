package net.jtownson.swakka.jsonprotocol

import net.jtownson.swakka.OpenApiModel.ResponseValue
import net.jtownson.swakka.jsonprotocol.ResponsesJsonProtocol._
import net.jtownson.swakka.jsonschema.SchemaWriter
import net.jtownson.swakka.jsonschema.SchemaWriter._
import org.scalatest.FlatSpec
import org.scalatest.Matchers._
import shapeless.{::, HNil}
import spray.json.{JsObject, JsString, _}

class ResponsesJsonProtocolSpec extends FlatSpec {

  object UserCode {

    case class Success(id: String)

    case class Error(msg: String)

    implicit val successSchema: SchemaWriter[Success] = schemaWriter(Success)
    implicit val errorSchema: SchemaWriter[Error] = schemaWriter(Error)
  }

  import UserCode._

  "Responses JsonProtocol" should "write HNil as empty" in {
    val hn: HNil = HNil
    hn.toJson shouldBe JsObject()
  }

  "Responses JsonProtocol" should "write a complex response" in {

    type Responses = ResponseValue[Success] :: ResponseValue[String] :: ResponseValue[Error] :: HNil

    val responses: Responses =
      ResponseValue[Success](200, "ok") :: ResponseValue[String](404, "not found") :: ResponseValue[Error](500, "server error") :: HNil

    val expectedJson =
      JsObject(
        "200" -> JsObject(
          "description" -> JsString("ok"),
          "schema" ->
            JsObject(
              "type" -> JsString("object"),
              "properties" -> JsObject(
                "id" -> JsObject(
                  "type" -> JsString("string"))
              )
            )
        ),
        "404" -> JsObject(
          "description" -> JsString("not found"),
          "schema" ->
            JsObject(
              "type" -> JsString("string"))
        ),
        "500" -> JsObject(
          "description" -> JsString("server error"),
          "schema" ->
            JsObject(
              "type" -> JsString("object"),
              "properties" -> JsObject(
                "msg" -> JsObject(
                  "type" -> JsString("string"))
              )
            )
        )
      )

    responses.toJson shouldBe expectedJson
  }
}
