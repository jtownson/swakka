import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods.GET
import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import shapeless.{::, HNil}


// Shows how to create
// an endpoint that accepts a query parameter

// Usage: curl -i http://localhost:8080/greet?name=John

// API model
import net.jtownson.swakka.OpenApiModel._
import net.jtownson.swakka.model.Parameters.QueryParameter
import net.jtownson.swakka.model.Responses.ResponseValue

// Akka http route generation
import net.jtownson.swakka.RouteGen

// Serialization of swagger.json
import net.jtownson.swakka.OpenApiJsonProtocol._


object Greeter1 extends App {

  implicit val system = ActorSystem()
  implicit val mat = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  type Params = QueryParameter[String] :: HNil
  type StringResponse = ResponseValue[String, HNil]

  type Paths = PathItem[Params, StringResponse] :: HNil

  val greet: Params => Route = {
    case (nameParameter :: HNil) =>
      complete(s"Hello ${nameParameter.value}!")
  }

  val api =
    OpenApi(paths =
      PathItem(
        path = "/greet",
        method = GET,
        operation = Operation[Params, StringResponse](
          parameters = QueryParameter[String]('name) :: HNil,
          responses = ResponseValue[String, HNil]("200", "ok"),
          endpointImplementation = greet
        )
      ) ::
        HNil
    )

  val route: Route = RouteGen.openApiRoute(api, includeSwaggerRoute = true)

  val bindingFuture = Http().bindAndHandle(
    route,
    "localhost",
    8080)
}
