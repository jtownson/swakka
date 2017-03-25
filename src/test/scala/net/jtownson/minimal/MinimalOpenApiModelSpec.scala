package net.jtownson.minimal

import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.HttpMethods.GET
import akka.http.scaladsl.model.StatusCodes.OK
import akka.http.scaladsl.testkit.{RouteTest, TestFrameworkInterface}
import net.jtownson.minimal.MinimalOpenApiModel.{OpenApiModel, Operation, PathItem, ResponseValue}
import net.jtownson.minimal.RouteGen._
import org.scalamock.scalatest.MockFactory
import org.scalatest.FlatSpec
import org.scalatest.Matchers._
import org.scalatest.prop.TableDrivenPropertyChecks._
import spray.json.JsObject

class MinimalOpenApiModelSpec extends FlatSpec with MockFactory with RouteTest with TestFrameworkInterface {

  val f = mockFunction[Map[Symbol, String], ToResponseMarshallable]

  private val defaultItem: PathItem[String] = PathItem(GET, Operation(List(), ResponseValue(200), f))

  val requestModels = Table[String, String, OpenApiModel[String]] (
    ("testcase name", "requestPath", "model"),
    ("index page", "/", OpenApiModel("/", defaultItem)),
    ("simple path", "/ruok", OpenApiModel("/ruok", defaultItem)),
    ("missing base path", "/ruok", OpenApiModel("ruok", defaultItem)),
    ("complex path", "/ruok/json", OpenApiModel("ruok/json", defaultItem))
  )

  forAll(requestModels) { (testcaseName, requestPath, apiModel) =>
    testcaseName should "convert to a complete akka Route" in {

      f expects Map[Symbol, String]('method -> "HttpMethod(GET)", 'path -> requestPath) returning "YES"

      val route = openApiRoute(apiModel)

      Get(requestPath) ~> route ~> check {
        status shouldBe OK
        responseAs[String] shouldBe "YES"
      }
    }
  }

  val jsonModels = Table[String, OpenApiModel[String], JsObject] (
    ("testcase name", "model", "expected swagger"),
    ("index page", OpenApiModel("/", defaultItem), JsObject())
  )

  forAll(jsonModels) { (testcaseName, apiModel, expectedSwagger) =>
    testcaseName should "convert to swagger json" in {

    }

  }
  override def failTest(msg: String): Nothing = throw new AssertionError(msg)
}
