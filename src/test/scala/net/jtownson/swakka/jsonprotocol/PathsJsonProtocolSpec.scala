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

class PathsJsonProtocolSpec extends FlatSpec {

  import ConvertibleToDirective0._
  import net.jtownson.swakka.OpenApiJsonProtocol._

  private val endpointImpl: HttpRequest => ToResponseMarshallable = (_: HttpRequest) => ???

  "JsonProtocol" should "write a parameterless endpoint" in {

    type Responses = ResponseValue[String]

    val endpoint = PathItem[HNil, Responses]("/ruok", Endpoint[HNil, Responses](
      GET, Operation(HNil, ResponseValue[String](200), endpointImpl)))

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

    val endpoint = PathItem[HNil, HNil]("/ruok", Endpoint[HNil, HNil](
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
    type Responses = ResponseValue[String]
    type Paths = PathItem[Params, Responses]

    val endpoint: PathItem[Params, Responses] = PathItem(
      "/ruok", Endpoint(GET, Operation(QueryParameter[String]('q) :: HNil, ResponseValue[String](200), endpointImpl)))

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
  type StringResponse = ResponseValue[String]
  type Paths = PathItem[OneIntParam, StringResponse] :: PathItem[OneStrParam, StringResponse] :: HNil

  it should "write a simple swagger definition" in {
    val api: OpenApi[Paths] =
      OpenApi(paths =
        PathItem[OneIntParam, StringResponse](
          path = "/app/e1",
          Endpoint(
            method = GET,
            operation = Operation(
              parameters = QueryParameter[Int]('q) :: HNil,
              responses = ResponseValue[String](200),
              endpointImplementation = endpointImpl
            )
          )
        ) ::
          PathItem[OneStrParam, StringResponse](
            path = "/app/e2",
            Endpoint(
              method = GET,
              operation = Operation(
                parameters = QueryParameter[String]('q) :: HNil,
                responses = ResponseValue[String](200),
                endpointImplementation = endpointImpl
              )
            )
          ) :: HNil
      )

    val expectedJson = JsObject(
      "swagger" -> JsString("2.0"),
      "info" -> JsObject(
        "title" -> JsString(""),
        "version" -> JsString("")
      ),
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

    apiFormat[Paths].write(api) shouldBe expectedJson
  }

  it should "write an empty swagger definition" in {
    val api = OpenApi[HNil](paths = HNil)
    val expectedJson = JsObject(
      "swagger" -> JsString("2.0"),
      "info" -> JsObject(
        "title" -> JsString(""),
        "version" -> JsString("")
      ),
      "paths" -> JsObject()
    )

    apiFormat[HNil].write(api) shouldBe expectedJson
  }
}
