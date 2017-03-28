package net.jtownson.minimal

import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.HttpMethods.GET
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.StatusCodes.OK
import akka.http.scaladsl.server.MalformedQueryParamRejection
import akka.http.scaladsl.testkit.{RouteTest, TestFrameworkInterface}
import net.jtownson.minimal.MinimalOpenApiModel._
import net.jtownson.minimal.RouteGen._
import org.scalamock.scalatest.MockFactory
import org.scalatest.FlatSpec
import org.scalatest.Matchers._
import org.scalatest.Inside._
import org.scalatest.prop.TableDrivenPropertyChecks._
import spray.json.JsObject
import QueryParamConversions._

class MinimalOpenApiModelSpec extends FlatSpec with MockFactory with RouteTest with TestFrameworkInterface {

  val f = mockFunction[HttpRequest, ToResponseMarshallable]

  private val defaultItem: PathItem[String, String] = PathItem(GET, Operation(List(), ResponseValue(200), f))

  private val itemWithQueryParam: PathItem[String, String] = PathItem(
    GET, Operation(List(QueryParameter('q)), ResponseValue(200), f))

  val requestModels = Table[String, HttpRequest, OpenApiModel[String, String], ToResponseMarshallable](
    ("testcase name", "request", "model", "response"),
    ("index page", get("/"), OpenApiModel("/", defaultItem), "YES"),
    ("simple path", get("/ruok"), OpenApiModel("/ruok", defaultItem), "YES"),
    ("missing base path", get("/ruok"), OpenApiModel("ruok", defaultItem), "YES"),
    ("complex path", get("/ruok/json"), OpenApiModel("ruok/json", defaultItem), "YES"),
    ("echo query", get("/app?q=x"), OpenApiModel("/app", itemWithQueryParam), "x")
  )


  forAll(requestModels) { (testcaseName, request, apiModel, response) =>
    testcaseName should "convert to a complete akka Route" in {

      f expects request returning "YES"

      val route = openApiRoute(apiModel)

      request ~> route ~> check {
        status shouldBe OK
        responseAs[String] shouldBe "YES"
      }
    }
  }

  val itemWithIntParam: PathItem[Int, String] = PathItem(
    GET, Operation(List(QueryParameter[Int]('q)), ResponseValue(200), f))

  "int params that are NOT ints" should "be rejected" in {

    val request = get("/app?q=x")

    val route = openApiRoute(OpenApiModel("/app", itemWithIntParam))

    request ~> route ~> check {
      inside (rejection) { case MalformedQueryParamRejection(parameterName, _, _) =>
        parameterName shouldBe "q"
      }
    }
  }

  "int params that are ints" should "be passed through" in {

    val request = get("/app?q=10")

    f expects request returning "x"

    val route = openApiRoute(OpenApiModel("/app", itemWithIntParam))

    request ~> route ~> check {
      status shouldBe OK
      responseAs[String] shouldBe "x"
    }
  }

  private def get(path: String): HttpRequest = {
    Get(s"http://example.com$path")
  }

  val jsonModels = Table[String, OpenApiModel[String, String], JsObject](
    ("testcase name", "model", "expected swagger"),
    ("index page", OpenApiModel("/", defaultItem), JsObject())
  )

  forAll(jsonModels) { (testcaseName, apiModel, expectedSwagger) =>
    testcaseName should "convert to swagger json" in {

    }

  }

  override def failTest(msg: String): Nothing = throw new AssertionError(msg)
}
