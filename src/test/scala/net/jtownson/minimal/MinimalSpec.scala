package net.jtownson.minimal

import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.HttpMethods.GET
import akka.http.scaladsl.model.StatusCodes.OK
import akka.http.scaladsl.testkit.{RouteTest, TestFrameworkInterface}
import net.jtownson.minimal.MinimalOpenApiModel.{Operation, _}
import net.jtownson.minimal.RouteGen._
import org.scalamock.scalatest.MockFactory
import org.scalatest.FlatSpec
import org.scalatest.Matchers._
import org.scalatest.prop.TableDrivenPropertyChecks._

class MinimalSpec extends FlatSpec with MockFactory with RouteTest with TestFrameworkInterface {

  val f1 = mockFunction[Map[Symbol, String], ToResponseMarshallable]

  private val defaultItem = PathItem(GET, Operation(List(), f1))

  val requestModels = Table[String, String, OpenApiModel] (
    ("testcase name", "requestPath", "model"),
    ("index page", "/", OpenApiModel("/", defaultItem)),
    ("simple path", "/ruok", OpenApiModel("/ruok", defaultItem)),
    ("missing base path", "/ruok", OpenApiModel("ruok", defaultItem)),
    ("complex path", "/ruok/json", OpenApiModel("ruok/json", defaultItem))
  )

  forAll(requestModels) { (testcaseName, requestPath, apiModel) =>
    testcaseName should "complete" in {

      f1 expects Map[Symbol, String]('method -> "HttpMethod(GET)", 'path -> requestPath) returning "YES"

      val route = openApiRoute(apiModel)

      Get(requestPath) ~> route ~> check {
        status shouldBe OK
        responseAs[String] shouldBe "YES"
      }
    }
  }

  override def failTest(msg: String): Nothing = throw new AssertionError(msg)
}
