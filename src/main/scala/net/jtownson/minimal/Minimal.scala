package net.jtownson.minimal

import akka.http.scaladsl.model.HttpMethod
import akka.http.scaladsl.model.HttpMethods.GET
import akka.http.scaladsl.server._

// Model supporting a single endpoint with query params.
// Good enough to verify principles in the code.
object MinimalOpenApiModel {

  sealed trait Parameter

  case class QueryParameter(name: Symbol)

  case class Operation(parameters: Seq[Parameter] = Nil)

  case class PathItem(method: HttpMethod, operation: Operation)

  case class OpenApiModel(requestPath: String, pathItem: PathItem)

}

object RouteGen {

  import MinimalOpenApiModel._
  import akka.http.scaladsl.server.Directives._

  def routeGen(model: OpenApiModel): Directive1[Map[Symbol, String]] =
    routeGen(model.requestPath, model.pathItem.method, model.pathItem.operation)

  private def routeGen(requestPath: String, requestMethod: HttpMethod, operation: Operation): Directive1[Map[Symbol, String]] =
    httpMethod(requestMethod) & PathHandling.akkaPath(requestPath) & QueryStringHandling.parameters(operation)

  private val httpMethod = Map(GET -> get)

  object PathHandling {

    private val notBlank = (s: String) => ! s.trim.isEmpty

    private def splitPath(requestPath: String): List[String] =
      requestPath.split("/").filter(notBlank).toList

    def akkaPath(requestPath: String): Directive[Unit] = {

      def loop(paths: List[String]): PathMatcher[Unit] = paths match {
        case Nil => PathMatchers.Neutral
        case pathSegment :: Nil => PathMatcher(pathSegment)
        case pathSegment :: tail => pathSegment / loop(tail)
      }

      val pathMatcher: PathMatcher[Unit] = loop(splitPath(requestPath))

      path(pathMatcher)
    }
  }

  object QueryStringHandling {
    private val pNil: Directive1[Map[Symbol, String]] = pass.tmap(_ => Map[Symbol, String]())

    private val mapAppend: ((Map[Symbol, String], Map[Symbol, String])) => Map[Symbol, String] = tl => tl._1 ++ tl._2

    private val appendParameter: (Directive1[Map[Symbol, String]], QueryParameter) => Directive1[Map[Symbol, String]] =
      (qps: Directive1[Map[Symbol, String]], qp: QueryParameter) => {

        val p: Directive1[String] = parameter(qp.name)
        val pl1: Directive1[Map[Symbol, String]] = p.map[Map[Symbol, String]](s => Map(qp.name -> s))

        (pl1 & qps).tmap(mapAppend)
      }

    private val onlyQueryParams: Parameter => Seq[QueryParameter] = {
      case q: QueryParameter => List(q)
      case _ => Nil
    }

    def parameters(o: Operation): Directive1[Map[Symbol, String]] =
      o.parameters.flatMap(onlyQueryParams).foldLeft(pNil)(appendParameter)

  }

}
