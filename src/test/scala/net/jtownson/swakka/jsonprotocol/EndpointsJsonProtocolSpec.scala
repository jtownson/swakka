package net.jtownson.swakka.jsonprotocol

import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.HttpMethods.GET
import akka.http.scaladsl.model.HttpRequest
import net.jtownson.swakka.OpenApiModel._
import net.jtownson.swakka.routegen.ConvertibleToDirective0
import org.scalatest.FlatSpec
import org.scalatest.Matchers._
import shapeless.{::, HNil}
import spray.json.{JsArray, JsFalse, JsObject, JsString, _}

class EndpointsJsonProtocolSpec extends FlatSpec {

  import ConvertibleToDirective0._
  import net.jtownson.swakka.OpenApiJsonProtocol._

  private val endpointImpl: HttpRequest => ToResponseMarshallable = (_: HttpRequest) => ???

  "JsonProtocol" should "write a parameterless endpoint" in {

    type Responses = ResponseValue[String] :: HNil

    val endpoint = Endpoint[HNil, Responses]("/ruok", PathItem[HNil, Responses](
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

    endpoint.toJson shouldBe expectedSwagger
  }

  it should "write a responseless endpoint as an empty object" in {

    val endpoint = Endpoint[HNil, HNil]("/ruok", PathItem[HNil, HNil](
      GET, Operation(HNil, HNil, endpointImpl)))

    val expectedSwagger = JsObject(
      "/ruok" -> JsObject(
        "get" -> JsObject()
      )
    )

    endpoint.toJson shouldBe expectedSwagger
  }

  it should "write an endpoint with a parameter" in {

    type Params = QueryParameter[String] :: HNil
    type Responses = ResponseValue[String] :: HNil
    type Endpoints = Endpoint[Params, Responses] :: HNil

    val endpoint: Endpoint[Params, Responses] = Endpoint(
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

    endpoint.toJson shouldBe expectedSwagger
  }

  type OneIntParam = QueryParameter[Int] :: HNil
  type OneStrParam = QueryParameter[String] :: HNil
  type StringResponse = ResponseValue[String] :: HNil
  type Endpoints = Endpoint[OneIntParam, StringResponse] :: Endpoint[OneStrParam, StringResponse] :: HNil

  it should "write a simple swagger definition" in {
    val api: OpenApi[Endpoints] =
      OpenApi(
        Endpoint[OneIntParam, StringResponse](
          path = "/app/e1",
          PathItem(
            method = GET,
            operation = Operation(
              parameters = QueryParameter[Int]('q) :: HNil,
              responses = ResponseValue[String](200) :: HNil,
              endpointImplementation = endpointImpl
            )
          )
        ) ::
          Endpoint[OneStrParam, StringResponse](
            path = "/app/e2",
            PathItem(
              method = GET,
              operation = Operation(
                parameters = QueryParameter[String]('q) :: HNil,
                responses = ResponseValue[String](200) :: HNil,
                endpointImplementation = endpointImpl
              )
            )
          ) :: HNil
      )

    val expectedJson = JsObject(
      "paths" -> JsObject(
        "/app/e1" -> JsObject(
          "get" -> JsObject(
            "parameters" -> JsArray(
              JsObject(
                "name" -> JsString("q"),
                "in" -> JsString("query"),
                "description" -> JsString(""),
                "required" -> JsFalse,
                "type" -> JsString("integer")
              )),
            "responses" -> JsObject(
              "200" -> JsObject(
                "schema" -> JsObject(
                  "type" -> JsString("string")
                )
              )
            )
          )
        ),
        "/app/e2" -> JsObject(
          "get" -> JsObject(
            "parameters" -> JsArray(
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
    )

    apiFormat[Endpoints].write(api) shouldBe expectedJson
  }
}
