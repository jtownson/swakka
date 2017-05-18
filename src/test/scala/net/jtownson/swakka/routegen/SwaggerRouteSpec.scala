package net.jtownson.swakka.routegen

import akka.http.scaladsl.model.HttpMethods.GET
import akka.http.scaladsl.model.StatusCodes.OK
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.{RouteTest, TestFrameworkInterface}
import net.jtownson.swakka.OpenApiModel._
import net.jtownson.swakka.OpenApiJsonProtocol
import net.jtownson.swakka.model.Parameters.QueryParameter
import org.scalamock.scalatest.MockFactory
import org.scalatest.FlatSpec
import org.scalatest.Matchers._
import shapeless.{::, HNil}
import spray.json._

class SwaggerRouteSpec extends FlatSpec with MockFactory with RouteTest with TestFrameworkInterface {

  import ConvertibleToDirective._
  import OpenApiJsonProtocol._

  def f[Params] = mockFunction[Params, Route]

  type OneIntParam = QueryParameter[Int] :: HNil
  type OneStrParam = QueryParameter[String] :: HNil

  type StringResponse = ResponseValue[String, HNil]

  type Paths = PathItem[OneIntParam, StringResponse] :: PathItem[OneStrParam, StringResponse] :: HNil

  "Swagger route" should "return a swagger file" in {
    val api =
      OpenApi(paths =
        PathItem[OneIntParam, StringResponse](
          path = "/app/e1",
          method = GET,
          operation = Operation(
            parameters = QueryParameter[Int]('q) :: HNil,
            responses = ResponseValue[String, HNil]("200", "ok"),
            endpointImplementation = f
          )
        ) ::
        PathItem[OneStrParam, StringResponse](
          path = "/app/e2",
          method = GET,
          operation = Operation(
            parameters = QueryParameter[String]('q) :: HNil,
            responses = ResponseValue[String, HNil]("200", "ok"),
            endpointImplementation = f
          )
        ) ::
        HNil
      )

    val route = SwaggerRoute.swaggerRoute(api)

    val request = Get(s"http://example.com/swagger.json")

    request ~> route ~> check {
      status shouldBe OK
      responseAs[String] shouldBe api.toJson.prettyPrint
    }
  }

  override def failTest(msg: String): Nothing = throw new AssertionError(msg)
}
