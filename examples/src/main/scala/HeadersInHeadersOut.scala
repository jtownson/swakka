import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods.GET
import akka.http.scaladsl.model.StatusCodes.NoContent
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import net.jtownson.swakka.OpenApiJsonProtocol._
import net.jtownson.swakka.OpenApiModel._
import net.jtownson.swakka.RouteGen
import net.jtownson.swakka.model.Parameters.HeaderParameter
import net.jtownson.swakka.model.Responses.{Header, ResponseValue}
import net.jtownson.swakka.routegen.{CorsUseCases, SwaggerRouteSettings}
import shapeless.{::, HNil}

import scala.collection.immutable.Seq

// Shows how to declare
// 1. an endpoint that accepts parameters in headers and returns headers in the response
// 2. extract request information that you do not wish to declare in the swagger definition
//
// Usage: curl -i -H'x-header-in: 3.14' http://localhost:8080/
object HeadersInHeadersOut extends App {

  implicit val system = ActorSystem()
  implicit val mat = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  type Params = HeaderParameter[Double] :: HNil
  type Responses = ResponseValue[Unit, Header[Double]]

  type Paths = PathItem[Params, Responses]

  val corsHeaders = Seq(
    RawHeader("Access-Control-Allow-Origin", "*"),
    RawHeader("Access-Control-Allow-Methods", "GET"))

  val multiplyInputBy2: Params => Route = {

    case (HeaderParameter(number) :: HNil) => {

      val ret = (number * 2).toString

      complete(HttpResponse(
        NoContent,
        corsHeaders :+ RawHeader("x-header-out", ret))
      )
    }
  }

  val api =
    OpenApi(
      paths =
      PathItem(
        path = "/",
        method = GET,
        operation = Operation[Params, Responses](
          parameters = HeaderParameter[Double](Symbol("x-header-in")) :: HNil,
          responses = ResponseValue[Unit, Header[Double]](
            responseCode = "204",
            description = "the input x-header-in parameter will be multiplied by 2 and returned in x-header-out",
            headers = Header[Double](Symbol("x-header-out"), Some("the value of x-header-in multiplied by 2"))),
          endpointImplementation = multiplyInputBy2
        )
      )
    )

  val route: Route = RouteGen.openApiRoute(api, Some(SwaggerRouteSettings(
    corsUseCase = CorsUseCases.SpecificallyThese(corsHeaders))))

  val bindingFuture = Http().bindAndHandle(
    route,
    "localhost",
    8080)
}
