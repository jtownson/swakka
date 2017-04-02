package net.jtownson.minimal

import akka.http.scaladsl.model.HttpMethods.GET
import akka.http.scaladsl.model.HttpRequest
import net.jtownson.minimal.MinimalOpenApiModel._
import org.scalatest.FlatSpec
import org.scalatest.Matchers._
import shapeless.{::, HNil}
import spray.json.{JsObject, JsString}

class MinimalJsonProtocolSpec extends FlatSpec {

  import ConvertibleToDirective0._
  import MinimalJsonSchemaJsonProtocol._
  import ParametersJsonProtocol._

  private val endpointImpl = (_: HttpRequest) => ???

  "JsonProtocol" should "write a parameterless model" in {

    val apiModel = OpenApiModel("/ruok", PathItem[HNil, String](
      GET, Operation(HNil, ResponseValue(200), endpointImpl)))

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

    val openApiModelFormat = new MinimalJsonProtocol[HNil, String].openApiModelWriter

    openApiModelFormat.write(apiModel) shouldBe expectedSwagger
  }

  it should "write a model with a parameter" in {


  type Params = QueryParameter[String] :: HNil

  val apiModel = OpenApiModel(
    "/ruok", PathItem[Params, String](GET, Operation(QueryParameter[String]('q) :: HNil, ResponseValue(200), endpointImpl)))

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

    val openApiModelFormat = new MinimalJsonProtocol[Params, String].openApiModelWriter

    openApiModelFormat.write(apiModel) shouldBe expectedSwagger
  }
}
