package net.jtownson.swakka.routegen

import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.HttpMethods.GET
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.StatusCodes.OK
import akka.http.scaladsl.testkit.{RouteTest, TestFrameworkInterface}
import net.jtownson.swakka.OpenApiModel._
import net.jtownson.swakka.OpenApiJsonProtocol
import org.scalamock.scalatest.MockFactory
import org.scalatest.FlatSpec
import org.scalatest.Matchers._
import shapeless.{::, HNil}
import spray.json._

class SwaggerRouteSpec extends FlatSpec with MockFactory with RouteTest with TestFrameworkInterface {

  import ConvertibleToDirective0._
  import OpenApiJsonProtocol._

  val f = mockFunction[HttpRequest, ToResponseMarshallable]

  type OneIntParam = QueryParameter[Int] :: HNil
  type OneStrParam = QueryParameter[String] :: HNil

  type StringResponse = ResponseValue[String]

  type Endpoints = Endpoint[OneIntParam, StringResponse] :: Endpoint[OneStrParam, StringResponse] :: HNil

  "Swagger route" should "return a swagger file" in {
    val api =
      OpenApi(endpoints =
        Endpoint[OneIntParam, StringResponse](
          path = "/app/e1",
          PathItem(
            method = GET,
            operation = Operation(
              parameters = QueryParameter[Int]('q) :: HNil,
              responses = ResponseValue[String](200),
              endpointImplementation = f
            )
          )
        ) ::
          Endpoint[OneStrParam, StringResponse](
            path = "/app/e2",
            PathItem(
              method = GET,
              operation = Operation(
                parameters = QueryParameter[String]('q) :: HNil,
                responses = ResponseValue[String](200),
                endpointImplementation = f
              )
            )
          ) ::
        HNil
      )

    implicit val jsonProtocol = apiFormat[Endpoints]

    val route = SwaggerRoute.swaggerRoute(api)

    val request = Get(s"http://example.com/swagger.json")

    request ~> route ~> check {
      status shouldBe OK
      responseAs[String] shouldBe api.toJson.prettyPrint
    }
  }

  override def failTest(msg: String): Nothing = throw new AssertionError(msg)
}
