package net.jtownson.swakka.jsonprotocol

import akka.http.scaladsl.model.HttpMethods.{GET, POST}
import akka.http.scaladsl.server.Route
import net.jtownson.swakka.OpenApiModel._
import net.jtownson.swakka.routegen.ConvertibleToDirective._
import net.jtownson.swakka.OpenApiJsonProtocol._
import net.jtownson.swakka.model.Parameters.QueryParameter
import net.jtownson.swakka.model.Responses.ResponseValue
import net.jtownson.swakka.model.SecurityDefinitions.SecurityRequirement
import org.scalatest.FlatSpec
import org.scalatest.Matchers._
import shapeless.{::, HNil}
import spray.json.{JsArray, JsObject, JsString, _}

class PathsJsonProtocolSpec extends FlatSpec {


  private def endpointImpl[Params]: Params => Route = _ => ???

  "JsonProtocol" should "write a parameterless pathitem" in {

    type Responses = ResponseValue[String, HNil]

    val pathItem = PathItem(
      path = "/ruok",
      method = POST,
      operation = Operation[HNil, ResponseValue[String, HNil]](
        parameters = HNil,
        responses = ResponseValue("200", "ok"),
        endpointImplementation = endpointImpl))

    val expectedSwagger = JsObject(
      "/ruok" -> JsObject(
        "post" -> JsObject(
          "responses" -> JsObject(
            "200" -> JsObject(
              "description" -> JsString("ok"),
              "schema" -> JsObject(
                "type" -> JsString("string")
              )
            )
          )
        )
      )
    )

    pathItem.toJson shouldBe expectedSwagger
  }

  it should "write a responseless pathItem as an empty object" in {

    val pathItem = PathItem(
      path = "/ruok",
      method = GET,
      operation = Operation[HNil, HNil](
        parameters = HNil,
        responses = HNil,
        endpointImplementation = endpointImpl))

    val expectedSwagger = JsObject(
      "/ruok" -> JsObject(
        "get" -> JsObject()
      )
    )

    pathItem.toJson shouldBe expectedSwagger
  }

  it should "write an pathItem with a parameter" in {

    type Params = QueryParameter[String] :: HNil
    type Responses = ResponseValue[String, HNil]
    type Paths = PathItem[Params, Responses]

    val pathItem: PathItem[Params, Responses] = PathItem(
      path = "/ruok",
      method = GET,
      operation = Operation(
        parameters = QueryParameter[String]('q) :: HNil,
        responses = ResponseValue[String, HNil]("200", "ok"),
        endpointImplementation = endpointImpl))

    val expectedSwagger = JsObject(
      "/ruok" -> JsObject(
        "get" -> JsObject(
          "parameters" ->
            JsArray(
              JsObject(
                "name" -> JsString("q"),
                "in" -> JsString("query"),
                "required" -> JsTrue,
                "type" -> JsString("string")
              )),
          "responses" -> JsObject(
            "200" -> JsObject(
              "description" -> JsString("ok"),
              "schema" -> JsObject(
                "type" -> JsString("string")
              )
            )
          )
        )
      )
    )

    pathItem.toJson shouldBe expectedSwagger
  }

  type OneIntParam = QueryParameter[Int] :: HNil
  type OneStrParam = QueryParameter[String] :: HNil
  type StringResponse = ResponseValue[String, HNil]
  type Paths = PathItem[OneIntParam, StringResponse] :: PathItem[OneStrParam, StringResponse] :: HNil

  it should "write a simple swagger definition" in {
    val api: OpenApi[Paths, HNil] =
      OpenApi(paths =
        PathItem[OneIntParam, StringResponse](
          path = "/app/e1",
          method = GET,
          operation = Operation(
            parameters = QueryParameter[Int]('q) :: HNil,
            responses = ResponseValue[String, HNil]("200", "ok"),
            endpointImplementation = endpointImpl
          )
        )
          ::
          PathItem[OneStrParam, StringResponse](
            path = "/app/e2",
            method = GET,
            operation = Operation(
              parameters = QueryParameter[String]('q) :: HNil,
              responses = ResponseValue[String, HNil]("200", "ok"),
              endpointImplementation = endpointImpl
            )
          )
          :: HNil
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
                "required" -> JsTrue,
                "type" -> JsString("integer"),
                "format" -> JsString("int32")
              )),
            "responses" -> JsObject(
              "200" -> JsObject(
                "description" -> JsString("ok"),
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
                "required" -> JsTrue,
                "type" -> JsString("string")
              )),
            "responses" -> JsObject(
              "200" -> JsObject(
                "description" -> JsString("ok"),
                "schema" -> JsObject(
                  "type" -> JsString("string")
                )
              )
            )
          )
        )
      )
    )

    apiFormat[Paths, HNil].write(api) shouldBe expectedJson
  }

  it should "write an empty swagger definition" in {
    val api = OpenApi[HNil, HNil](paths = HNil)
    val expectedJson = JsObject(
      "swagger" -> JsString("2.0"),
      "info" -> JsObject(
        "title" -> JsString(""),
        "version" -> JsString("")
      ),
      "paths" -> JsObject()
    )

    apiFormat[HNil, HNil].write(api) shouldBe expectedJson
  }

  it should "write a swagger security definition with a security requirement" in {

    val api = OpenApi(
      paths =
        PathItem(
          path = "/app/e1",
          method = GET,
          operation = Operation(
            parameters = QueryParameter[Int]('q) :: HNil,
            responses = ResponseValue[String, HNil]("200", "ok"),
            security = Some(Seq(SecurityRequirement('auth, Seq("grant1", "grant2")))),
            endpointImplementation = endpointImpl
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
                "required" -> JsTrue,
                "type" -> JsString("integer"),
                "format" -> JsString("int32")
              )),
            "responses" -> JsObject(
              "200" -> JsObject(
                "description" -> JsString("ok"),
                "schema" -> JsObject(
                  "type" -> JsString("string")
                )
              )
            ),
            "security" -> JsArray(
              JsObject(
                "auth" -> JsArray(JsString("grant1"), JsString("grant2")
                )
              )
            )
          )
        )
      )
    )

    api.toJson shouldBe expectedJson
  }

  it should "combine path items where the path is equal" in {

    type Paths = PathItem[HNil, HNil] :: PathItem[HNil, HNil] :: HNil

    val paths: Paths =
      PathItem[HNil, HNil](
        path = "/app",
        method = GET,
        operation = Operation(
          endpointImplementation = endpointImpl
        )
      ) ::
        PathItem[HNil, HNil](
          path = "/app",
          method = POST,
          operation = Operation(
            endpointImplementation = endpointImpl
          )
        ) ::
        HNil

    val expectedJson =
      JsObject(
        "/app" -> JsObject(
          "get" -> JsObject(),
          "post" -> JsObject()
        )
      )

    paths.toJson shouldBe expectedJson
  }
}
