package net.jtownson.minimal

import akka.http.scaladsl.model.HttpMethods.GET
import net.jtownson.minimal.MinimalOpenApiModel._
import org.scalatest.FlatSpec
import org.scalatest.Matchers._
import org.scalatest.prop.TableDrivenPropertyChecks._
import spray.json.{JsObject, JsString}

class MinimalJsonProtocolSpec extends FlatSpec {

  import MinimalJsonSchemaJsonProtocol._

  private val endpointImpl = (_: Map[Symbol, String]) => ???

  private implicit val openApiModelFormat = new MinimalJsonProtocol[String].openApiModelWriter

  private val ruokModel: OpenApiModel[String] = OpenApiModel(
    "/ruok", PathItem(
      GET, Operation(Nil,
        ResponseValue(200), endpointImpl)))

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

  private val jsonModels = Table[String, OpenApiModel[String], JsObject](
    ("testcase name", "model", "expected swagger"),
    ("index page", ruokModel, ruokSwaggerJson)
  )

  forAll(jsonModels) { (testcaseName, apiModel, expectedSwagger) =>
    testcaseName should "convert to swagger json" in {
      openApiModelFormat.write(apiModel) shouldBe expectedSwagger
    }
  }
}
