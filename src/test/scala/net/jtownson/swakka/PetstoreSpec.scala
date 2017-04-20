package net.jtownson.swakka

import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.testkit.{RouteTest, TestFrameworkInterface}
import net.jtownson.swakka.OpenApiModel.OpenApi
import net.jtownson.swakka.RouteGen.openApiRoute
import net.jtownson.swakka.model.{Info, Licence}
import org.scalamock.scalatest.MockFactory
import org.scalatest.FlatSpec
import org.scalatest.Matchers._
import shapeless.HNil
import spray.json.{JsObject, JsString}

class PetstoreSpec extends FlatSpec with MockFactory with RouteTest with TestFrameworkInterface {

  val apiInfo = Info(version = "1.0.0", title = "Swagger Petstore", licence = Some(Licence(name = "MIT")))

  import OpenApiJsonProtocol._

  "Swakka" should "support the petstore example" in {

    type Endpoints = HNil

    val petstoreApi = OpenApi[Endpoints](
      info = apiInfo,
      host = Some("petstore.swagger.io"),
      endpoints = HNil)
    implicit val jsonFormat = apiFormat[Endpoints]
    val apiRoutes = openApiRoute(petstoreApi, includeSwaggerRoute = true)

    val expectedJson = JsObject(
      "swagger" -> JsString("2.0"),
      "info" -> JsObject(
        "title" -> JsString("Swagger Petstore"),
        "version" -> JsString("1.0.0"),
        "licence" -> JsObject(
          "name" -> JsString("MIT")
        )
      ),
      "host" -> JsString("petstore.swagger.io"),
      "paths" -> JsObject()
    )

    get("/swagger.json") ~> apiRoutes ~> check {
      responseAs[String] shouldBe expectedJson.prettyPrint
    }
  }

  private def get(path: String): HttpRequest = {
    Get(s"http://petstore.swagger.io$path")
  }

  override def failTest(msg: String): Nothing = throw new AssertionError(msg)
}
