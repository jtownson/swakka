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

  def openApiRoute[Paths](api: OpenApi[Paths], includeSwaggerRoute: Boolean = false)
                         (implicit ev1: RouteGen[Paths], ev2: JsonFormat[OpenApi[Paths]]): Route =
    hostDirective(api.host) {
      schemesDirective(api.schemes) {
        basePathDirective(api.basePath) {
          if (includeSwaggerRoute)
            ev1.toRoute(api.paths) ~ SwaggerRoute.swaggerRoute(api)
          else
            ev1.toRoute(api.paths)
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

      val paramsDirective: Directive1[Params] = ev.convertToDirective(modelPath, operation.parameters)

      paramsDirective { params =>

        extractRequest { request =>

          complete(operation.endpointImplementation(params, request))
        }
      }
    }
  }
}
