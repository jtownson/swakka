package net.jtownson.swakka

import akka.http.scaladsl.model.HttpMethod
import akka.http.scaladsl.server._
import shapeless.{HList, HNil, :: => hcons}
import OpenApiModel._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.directives.RouteDirectives
import net.jtownson.swakka.routegen.SwaggerRoute.swaggerRoute
import net.jtownson.swakka.routegen._
import spray.json.JsonFormat

trait RouteGen[T] {
  def toRoute(t: T): Route
}

object RouteGen {

  def openApiRoute[Paths, SecurityDefinitions]
  (api: OpenApi[Paths, SecurityDefinitions], swaggerRouteSettings: Option[SwaggerRouteSettings] = None)
  (implicit ev1: RouteGen[Paths], ev2: JsonFormat[OpenApi[Paths, SecurityDefinitions]]): Route =
    hostDirective(api.host) {
      schemesDirective(api.schemes) {
        basePathDirective(api.basePath) {
          swaggerRouteSettings match {
            case Some(settings) => ev1.toRoute(api.paths) ~ swaggerRoute(api, settings)
            case None => ev1.toRoute(api.paths)
          }
        }
      }
    }

  implicit def hconsRouteGen[H, T <: HList](implicit ev1: RouteGen[H], ev2: RouteGen[T]): RouteGen[hcons[H, T]] =
    (l: hcons[H, T]) => ev1.toRoute(l.head) ~ ev2.toRoute(l.tail)

  implicit def pathItemRouteGen[Params <: HList : ConvertibleToDirective, Responses]: RouteGen[PathItem[Params, Responses]] =
    (pathItem: PathItem[Params, Responses]) => pathItemRoute(pathItem)

  implicit val hNilRouteGen: RouteGen[HNil] =
    _ => RouteDirectives.reject

  def pathItemRoute[Params <: HList : ConvertibleToDirective, Responses](pathItem: PathItem[Params, Responses]): Route =
    pathItemRoute(pathItem.method, pathItem.path, pathItem.operation)

  private def pathItemRoute[Params <: HList : ConvertibleToDirective, Responses]
  (httpMethod: HttpMethod, modelPath: String, operation: Operation[Params, Responses])
  (implicit ev: ConvertibleToDirective[Params]) = {

    method(httpMethod) {

      ev.convertToDirective(modelPath, operation.parameters) { params =>

        operation.endpointImplementation(params)
      }
    }
  }
}
