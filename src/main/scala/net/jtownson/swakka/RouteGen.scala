package net.jtownson.swakka

import akka.http.scaladsl.model.HttpMethod
import akka.http.scaladsl.server._
import shapeless.{HList, HNil, :: => hcons}
import OpenApiModel._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.directives.RouteDirectives
import net.jtownson.swakka.RouteGen.PathHandling._
import net.jtownson.swakka.routegen.{ConvertibleToDirective0, SwaggerRoute}
import spray.json.JsonFormat
import ConvertibleToDirective0._

trait RouteGen[T] {
  def toRoute(t: T): Route
}

object RouteGen {

  def openApiRoute[Endpoints](api: OpenApi[Endpoints], includeSwaggerRoute: Boolean = false)
                                      (implicit ev1: RouteGen[Endpoints], ev2: JsonFormat[OpenApi[Endpoints]]): Route =
    if (includeSwaggerRoute)
      ev1.toRoute(api.endpoints) ~ SwaggerRoute.swaggerRoute(api)
    else
      ev1.toRoute(api.endpoints)

  implicit def hconsRouteGen[H, T <: HList](implicit ev1: RouteGen[H], ev2: RouteGen[T]): RouteGen[hcons[H, T]] =
    (l: hcons[H, T]) => ev1.toRoute(l.head) ~ ev2.toRoute(l.tail)

  implicit def endpointRouteGen[Params: ConvertibleToDirective0, Responses]: RouteGen[Endpoint[Params, Responses]] =
    (e: Endpoint[Params, Responses]) => endpointRoute(e)

  implicit val hNilRouteGen: RouteGen[HNil] =
    _ => RouteDirectives.reject

  def endpointRoute[Params: ConvertibleToDirective0, Responses](endpoint: Endpoint[Params, Responses]): Route =
    endpointRoute(endpoint.pathItem.method, endpoint.path, endpoint.pathItem.operation)

  private def endpointRoute[Params: ConvertibleToDirective0, Responses](httpMethod: HttpMethod, modelPath: String, operation: Operation[Params, Responses]) = {

    method(httpMethod) {

      akkaPath(modelPath) {

        val directive = convertToDirective0(operation.parameters)

        directive {

          extractRequest { request =>

            complete(operation.endpointImplementation(request))
          }
        }
      }
    }
  }


  object PathHandling {
    private val notBlank = (s: String) => !s.trim.isEmpty

    private def splitPath(requestPath: String): List[String] =
      requestPath.split("/").filter(notBlank).toList

    def akkaPath(modelPath: String): Directive[Unit] = {

      def loop(paths: List[String]): PathMatcher[Unit] = paths match {
        case Nil => PathMatchers.Neutral
        case pathSegment :: Nil => PathMatcher(pathSegment)
        case pathSegment :: tail => pathSegment / loop(tail)
      }

      val pathMatcher: PathMatcher[Unit] = loop(splitPath(modelPath))

      path(pathMatcher)
    }
  }
}
