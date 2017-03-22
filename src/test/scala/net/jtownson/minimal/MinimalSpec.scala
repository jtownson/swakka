package net.jtownson.minimal

import akka.http.scaladsl.model.HttpMethods.GET
import akka.http.scaladsl.model.StatusCodes.OK
import akka.http.scaladsl.server.Directives.{complete, path}
import akka.http.scaladsl.testkit.{RouteTest, TestFrameworkInterface}
import net.jtownson.minimal.MinimalOpenApiModel.Operation
import net.jtownson.minimal.RouteGen._
import org.scalatest.{FlatSpec, Matchers}
import org.scalatest.prop.TableDrivenPropertyChecks._
import MinimalOpenApiModel._

class MinimalSpec extends FlatSpec with Matchers with RouteTest with TestFrameworkInterface {

  private val defaultItem = PathItem(GET, Operation(List()))

  val requestModels = Table[String, String, OpenApiModel] (
    ("testcase name", "requestPath", "model"),
    ("index page", "/", OpenApiModel("/", defaultItem)),
    ("simple ruok", "/ruok", OpenApiModel("ruok", defaultItem)),
    ("leading slash", "/ruok", OpenApiModel("/ruok", defaultItem)),
    ("complex path", "/ruok/json", OpenApiModel("ruok/json", defaultItem))
  )

  forAll(requestModels) { (testcaseName, requestPath, apiModel) =>
    testcaseName should "complete" in {

      val route = routeGen(apiModel) { m =>
        complete("YES")
      }

      Get(requestPath) ~> route ~> check {
        status shouldBe OK
        responseAs[String] shouldBe "YES"
      }
    }
  }

  override def failTest(msg: String): Nothing = throw new AssertionError(msg)
}
