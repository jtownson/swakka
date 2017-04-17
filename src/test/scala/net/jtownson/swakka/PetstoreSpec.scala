package net.jtownson.swakka

import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.testkit.{RouteTest, TestFrameworkInterface}
import net.jtownson.swakka.OpenApiModel.OpenApi
import net.jtownson.swakka.RouteGen.openApiRoute
import org.scalamock.scalatest.MockFactory
import org.scalatest.FlatSpec
import org.scalatest.Matchers._
import shapeless.HNil
import spray.json.{JsObject, JsString}

class PetstoreSpec extends FlatSpec with MockFactory with RouteTest with TestFrameworkInterface {

  import OpenApiJsonProtocol._

  "Swakka" should "support the petstore example" in {

    type Endpoints = HNil

    val petstoreApi = OpenApi[Endpoints](HNil)
    implicit val jsonFormat = apiFormat[Endpoints]
    val apiRoutes = openApiRoute(petstoreApi, includeSwaggerRoute = true)

    val expectedJson = JsObject(
      "swagger" -> JsString("2.0"),
      "paths" -> JsObject()
    )

    get("/swagger.json") ~> apiRoutes ~> check {
      responseAs[String] shouldBe expectedJson.prettyPrint
    }
  }

  private def get(path: String): HttpRequest = {
    Get(s"http://example.com$path")
  }

  override def failTest(msg: String): Nothing = throw new AssertionError(msg)
}
