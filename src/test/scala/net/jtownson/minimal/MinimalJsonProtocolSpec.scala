package net.jtownson.minimal

import akka.http.scaladsl.model.HttpMethods.GET
import akka.http.scaladsl.model.HttpRequest
import net.jtownson.minimal.MinimalOpenApiModel._
import org.scalatest.FlatSpec
import org.scalatest.Matchers._
import org.scalatest.prop.TableDrivenPropertyChecks._
import shapeless.HNil
import spray.json.{JsObject, JsString}

class MinimalJsonProtocolSpec extends FlatSpec {

  import ConvertibleToDirective0._
  import ParametersJsonProtocol._
  import MinimalJsonSchemaJsonProtocol._

  private val endpointImpl = (_: HttpRequest) => ???

  private implicit val openApiModelFormat = new MinimalJsonProtocol[HNil, String].openApiModelWriter

  private val ruokModel = OpenApiModel("/ruok", PathItem[HNil, String](
      GET, Operation(HNil, ResponseValue(200), endpointImpl)))

  val ruokSwaggerJson = JsObject(
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

  private val jsonModels = Table(
    ("testcase name", "model", "expected swagger"),
    ("index page", ruokModel, ruokSwaggerJson)
  )

  forAll(jsonModels) { (testcaseName, apiModel, expectedSwagger) =>
    testcaseName should "convert to swagger json" in {
      openApiModelFormat.write(apiModel) shouldBe expectedSwagger
    }
  }
}
