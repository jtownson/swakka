package net.jtownson.swakka

import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.HttpMethods.GET
import akka.http.scaladsl.model.HttpRequest
import net.jtownson.swakka.OpenApiModel._
import org.scalatest.FlatSpec
import org.scalatest.Matchers._
import shapeless.{::, HNil}
import spray.json.{JsArray, JsFalse, JsObject, JsString}

class OpenApiJsonProtocolSpec extends FlatSpec {

  import ConvertibleToDirective0._
  import ParametersJsonProtocol._
  import ResponsesJsonProtocol._

  private val endpointImpl: HttpRequest => ToResponseMarshallable = (_: HttpRequest) => ???

  "JsonProtocol" should "write a parameterless model" in {

    type Responses = ResponseValue[String] :: HNil

    val apiModel = Endpoint[HNil, Responses]("/ruok", PathItem[HNil, Responses](
      GET, Operation(HNil, ResponseValue[String](200) :: HNil, endpointImpl)))

    val expectedSwagger = JsObject(
      "/ruok" -> JsObject(
        "get" -> JsObject(
          "responses" -> JsObject(
            "200" -> JsObject(
              "schema" -> JsObject(
                "type" -> JsString("string")
              )
            )
          )
        )
      )
    )

    val openApiModelFormat = new OpenApiJsonProtocol[HNil, Responses].openApiModelWriter

    openApiModelFormat.write(apiModel) shouldBe expectedSwagger
  }

  it should "write a model with a parameter" in {

    type Params = QueryParameter[String] :: HNil
    type Responses = ResponseValue[String] :: HNil

    val apiModel: Endpoint[Params, Responses] = Endpoint(
      "/ruok", PathItem(GET, Operation(QueryParameter[String]('q) :: HNil, ResponseValue[String](200) :: HNil, endpointImpl)))

    val expectedSwagger = JsObject(
      "/ruok" -> JsObject(
        "get" -> JsObject(
          "parameters" ->
            JsArray(
              JsObject(
                "name" -> JsString("q"),
                "in" -> JsString("query"),
                "description" -> JsString(""),
                "required" -> JsFalse,
                "type" -> JsString("string")
              )),
          "responses" -> JsObject(
            "200" -> JsObject(
              "schema" -> JsObject(
                "type" -> JsString("string")
              )
            )
          )
        )
      )
    )

    val openApiModelFormat = new OpenApiJsonProtocol[Params, Responses].openApiModelWriter

    openApiModelFormat.write(apiModel) shouldBe expectedSwagger
  }

}
