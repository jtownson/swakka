package net.jtownson.minimal

import akka.http.scaladsl.model.HttpMethods.GET
import akka.http.scaladsl.model.StatusCodes.OK
import akka.http.scaladsl.server.Directives.{complete, path}
import akka.http.scaladsl.testkit.{RouteTest, TestFrameworkInterface}
import net.jtownson.minimal.MinimalOpenApiModel.Operation
import net.jtownson.minimal.RouteGen._
import org.scalatest.{FlatSpec, Matchers}

class MinimalSpec extends FlatSpec with Matchers with RouteTest with TestFrameworkInterface {

  "get simple path" should "complete" in {

    val apiModel = ("ruok", (GET, Operation(List())))

    val route = routeGen(apiModel) {
      complete("YES")
    }

//    val r2 = path("ruok") {
//
//    }
    Get("/ruok") ~> route ~> check {
      status shouldBe OK
      responseAs[String] shouldBe "YES"
    }
  }

  override def failTest(msg: String): Nothing = throw new AssertionError(msg)
}
