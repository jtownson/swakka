package net.jtownson.swakka.openapijson

import akka.http.scaladsl.model.HttpMethods.GET
import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json.{JsArray, JsObject, JsString, JsTrue, _}

import net.jtownson.swakka.openapimodel._
import net.jtownson.swakka.openapijson._

import org.scalatest.FlatSpec
import org.scalatest.Matchers._

import shapeless.HNil

class OpenApiJsonProtocolSpec extends FlatSpec {

  val dummyEndpoint: () => Route =
    () => complete("dummy")

  val dummyStringEndpoint: String => Route =
    _ => complete("dummy")

  val dummyIntEndpoint: Int => Route =
    _ => complete("dummy")

  "OpenApiJsonFormat" should "write a simple swagger definition" in {
    val api =
      OpenApi(paths =
        PathItem(
          path = "/app/e1",
          method = GET,
          operation = Operation(
            parameters = Tuple1(QueryParameter[Int]('q)),
            responses = ResponseValue[String, HNil]("200", "ok"),
            endpointImplementation = dummyIntEndpoint
          )
        )
          ::
          PathItem(
            path = "/app/e2",
            method = GET,
            operation = Operation(
              parameters = Tuple1(QueryParameter[String]('q)),
              responses = ResponseValue[String, HNil]("200", "ok"),
              endpointImplementation = dummyStringEndpoint
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

    api.toJson shouldBe expectedJson
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

    api.toJson shouldBe expectedJson
  }

  it should "write a swagger security definition with a security requirement" in {

    val api = OpenApi(
      paths =
        PathItem(
          path = "/app/e1",
          method = GET,
          operation = Operation(
            parameters = Tuple1(QueryParameter[Int]('q)),
            responses = ResponseValue[String, HNil]("200", "ok"),
            security = Some(Seq(SecurityRequirement('auth, Seq("grant1", "grant2")))),
            endpointImplementation = (_: Int) => complete("dummy")
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
}
