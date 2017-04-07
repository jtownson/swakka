package net.jtownson.swakka

import net.jtownson.swakka.OpenApiModel.ResponseValue
import net.jtownson.swakka.ResponsesJsonProtocol._
import net.jtownson.swakka.SchemaWriter._
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
    implicit val jfs: ResponseJsonFormat[ResponseValue[Success]] = caseClassResponseFormat1(Success)
    implicit val jfe: ResponseJsonFormat[ResponseValue[Error]] = caseClassResponseFormat1(Error)
  }

  import UserCode._

  "Responses JsonProtocol" should "write HNil as empty" in {
    val hn: HNil = HNil
    hn.toJson shouldBe JsArray()
  }

  "Responses JsonProtocol" should "write a complex response" in {

    type Responses = ResponseValue[Success] :: ResponseValue[String] :: ResponseValue[Error] :: HNil

    val responses: Responses =
      ResponseValue[Success](200) :: ResponseValue[String](404) :: ResponseValue[Error](500) :: HNil

    val expectedJson =
      JsObject(
        "200" -> JsObject(
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
          "schema" ->
            JsObject(
              "type" -> JsString("string"))
        ),
        "500" -> JsObject(
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
