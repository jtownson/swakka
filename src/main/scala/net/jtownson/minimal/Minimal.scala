package net.jtownson.minimal

import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.HttpMethod
import akka.http.scaladsl.model.HttpMethods.GET
import akka.http.scaladsl.server._

// Model supporting a single endpoint with query params.
// Good enough to verify principles in the code.
object MinimalOpenApiModel {

  sealed trait Parameter

  case class QueryParameter(name: Symbol)

  case class Operation(parameters: Seq[Parameter] = Nil,
                       endpointImplementation: Map[Symbol, String] => ToResponseMarshallable)

  case class PathItem(method: HttpMethod, operation: Operation)

  case class OpenApiModel(path: String, pathItem: PathItem)

}

object RouteGen {

  import MinimalOpenApiModel._
  import akka.http.scaladsl.server.Directives._
  import MethodHandling._
  import PathHandling._
  import QueryStringHandling._


  def openApiRoute(model: OpenApiModel): Route = {

    val (method, modelPath, operation) = (model.pathItem.method, model.path, model.pathItem.operation)

    requestMethod(method) { (requestMethod: HttpMethod) =>

      requestPath(modelPath) { (path: String) =>

        queryParameters(operation) { (parameterMap: Map[Symbol, String]) =>

          complete(operation.endpointImplementation(
            parameterMap + ('path -> path) + ('method -> method.toString()))) // TODO
        }
      }
    }
  }


  object MethodHandling {
    val httpMethod = Map(GET -> get)

    def requestMethod(httpMethod: HttpMethod): Directive1[HttpMethod] = {
      method(httpMethod) & extractMethod
    }
  }


  object PathHandling {

    def requestPath(modelPath: String): Directive1[String] = {
      akkaPath(modelPath) & extractMatchedPath.map(uriPath => uriPath.toString)
    }

    private val notBlank = (s: String) => ! s.trim.isEmpty

    private def splitPath(requestPath: String): List[String] =
      requestPath.split("/").filter(notBlank).toList

    private def akkaPath(modelPath: String): Directive[Unit] = {

      def loop(paths: List[String]): PathMatcher[Unit] = paths match {
        case Nil => PathMatchers.Neutral
        case pathSegment :: Nil => PathMatcher(pathSegment)
        case pathSegment :: tail => pathSegment / loop(tail)
      }

      val pathMatcher: PathMatcher[Unit] = loop(splitPath(modelPath))

      path(pathMatcher)
    }
  }


  object QueryStringHandling {

    def queryParameters(o: Operation): Directive1[Map[Symbol, String]] =
      o.parameters.flatMap(onlyQueryParams).foldLeft(pNil)(appendParameter)

    private val pNil: Directive1[Map[Symbol, String]] =
      pass.tmap(_ => Map[Symbol, String]())

    private val mapAppend: ((Map[Symbol, String], Map[Symbol, String])) => Map[Symbol, String] =
      tl => tl._1 ++ tl._2

    private val appendParameter: (Directive1[Map[Symbol, String]], QueryParameter) => Directive1[Map[Symbol, String]] =
      (qps, qp) => {

        val p: Directive1[String] = parameter(qp.name)
        val pl1: Directive1[Map[Symbol, String]] = p.map[Map[Symbol, String]](s => Map(qp.name -> s))

        (pl1 & qps).tmap(mapAppend)
      }

    private val onlyQueryParams: Parameter => Seq[QueryParameter] = {
      case q: QueryParameter => List(q)
      case _ => Nil
    }
  }
}
