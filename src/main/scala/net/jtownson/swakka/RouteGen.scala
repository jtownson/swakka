package net.jtownson.swakka

import akka.http.scaladsl.model.HttpMethod
import akka.http.scaladsl.server._
import shapeless.{HList, HNil, :: => hcons}
import OpenApiModel._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.directives.RouteDirectives
import net.jtownson.swakka.routegen.PathHandling.akkaPath
import net.jtownson.swakka.routegen._
import spray.json.JsonFormat

trait RouteGen[T] {
  def toRoute(t: T): Route
}

object RouteGen {

  def openApiRoute[Endpoints](api: OpenApi[Endpoints], includeSwaggerRoute: Boolean = false)
                             (implicit ev1: RouteGen[Endpoints], ev2: JsonFormat[OpenApi[Endpoints]]): Route =
    hostDirective(api.host) {
      schemesDirective(api.schemes) {
        basePathDirective(api.basePath) {
          if (includeSwaggerRoute)
            ev1.toRoute(api.endpoints) ~ SwaggerRoute.swaggerRoute(api)
          else
            ev1.toRoute(api.endpoints)
        }
      }
    }


  implicit def hconsRouteGen[H, T <: HList](implicit ev1: RouteGen[H], ev2: RouteGen[T]): RouteGen[hcons[H, T]] =
    (l: hcons[H, T]) => ev1.toRoute(l.head) ~ ev2.toRoute(l.tail)

  implicit def endpointRouteGen[Params <: HList : ConvertibleToDirective0, Responses]: RouteGen[Endpoint[Params, Responses]] =
    (e: Endpoint[Params, Responses]) => endpointRoute(e)

  implicit val hNilRouteGen: RouteGen[HNil] =
    _ => RouteDirectives.reject

  def endpointRoute[Params <: HList : ConvertibleToDirective0, Responses](endpoint: Endpoint[Params, Responses]): Route =
    endpointRoute(endpoint.pathItem.method, endpoint.path, endpoint.pathItem.operation)

  private def endpointRoute[Params <: HList : ConvertibleToDirective0, Responses]
  (httpMethod: HttpMethod, modelPath: String, operation: Operation[Params, Responses])
  (implicit ev: ConvertibleToDirective0[Params]) = {

    method(httpMethod) {

      akkaPath(modelPath) {

        ev.convertToDirective0(operation.parameters) {

          extractRequest { request =>

            complete(operation.endpointImplementation(request))
          }
        }
      }
    }
  }

}
