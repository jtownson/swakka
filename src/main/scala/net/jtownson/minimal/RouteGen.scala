package net.jtownson.minimal

import akka.http.scaladsl.model.HttpMethod
import akka.http.scaladsl.server._


object RouteGen {

  import ConvertibleToDirective0._
  import MinimalOpenApiModel._
  import PathHandling._
  import akka.http.scaladsl.server.Directives._

  def openApiRoute[I: ConvertibleToDirective0, T](model: OpenApiModel[I, T]): Route =
    openApiRoute(model.pathItem.method, model.path, model.pathItem.operation)

  private def openApiRoute[I: ConvertibleToDirective0, T](httpMethod: HttpMethod, modelPath: String, operation: Operation[I, T]) = {

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
