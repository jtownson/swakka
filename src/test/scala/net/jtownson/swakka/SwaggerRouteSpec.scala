package net.jtownson.swakka

import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.HttpMethods.GET
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.testkit.{RouteTest, TestFrameworkInterface}
import net.jtownson.swakka.OpenApiModel._
import org.scalamock.scalatest.MockFactory
import org.scalatest.FlatSpec
import org.scalatest.Matchers._
import shapeless.{::, HNil}

class SwaggerRouteSpec extends FlatSpec with MockFactory with RouteTest with TestFrameworkInterface {

  val f = mockFunction[HttpRequest, ToResponseMarshallable]

  type OneIntParam = QueryParameter[Int] :: HNil
  type OneStrParam = QueryParameter[String] :: HNil

  type StringResponse = ResponseValue[String] :: HNil

  "Swagger route" should "return a swagger file" in {
    val api =
      OpenApi(
        Endpoint[OneIntParam, StringResponse](
          path = "/app/e1",
          PathItem(
            method = GET,
            operation = Operation(
              parameters = QueryParameter[Int]('q) :: HNil,
              responses = ResponseValue[String](200) :: HNil,
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
                responses = ResponseValue[String](200) :: HNil,
                endpointImplementation = f
              )
            )
          ) ::
        HNil
      )

//    val route = swaggerRoute(api)
//
//    val request = Get(s"http://example.com/swagger.json")
//
//    request ~> route ~> check {
//      status shouldBe OK
//      responseAs[String] shouldBe "foo"
//    }
  }

  override def failTest(msg: String): Nothing = failTest(msg)
}
