package net.jtownson.minimal

import akka.http.scaladsl.model.HttpMethod
import akka.http.scaladsl.server.Directives._

import akka.http.scaladsl.server._
import net.jtownson.minimal.MinimalOpenApiModel.QueryParameter

trait QueryParamConverter[T] {
  def convert(qp: QueryParameter[T]): Directive0
}

object QueryParamConversions {

  implicit val stringConverter: QueryParamConverter[String] =
    (qp: QueryParameter[String]) => parameter(qp.name).tmap(_ => ())

  implicit val intConverter: QueryParamConverter[Int] =
    (qp: QueryParameter[Int]) => parameter(qp.name.as[Int]).tmap(_ => ())
}

object RouteGen {

  import MinimalOpenApiModel._
  import PathHandling._
  import QueryStringHandling._
  import akka.http.scaladsl.server.Directives._

  def openApiRoute[I: QueryParamConverter, T](model: OpenApiModel[I, T]): Route =
    openApiRoute(model.pathItem.method, model.path, model.pathItem.operation)

  private def openApiRoute[I : QueryParamConverter, T](httpMethod: HttpMethod, modelPath: String, operation: Operation[I, T]) = {

    method(httpMethod) {

      akkaPath(modelPath) {

        val queryParams = queryParameters(operation)

        queryParams {

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

  object QueryStringHandling {

    def queryParameters[I : QueryParamConverter, _](o: Operation[I, _]): Directive0 = {
      o.parameters.flatMap(onlyQueryParams).foldLeft(pNil)(appendParameter)
    }

    private val pNil: Directive0 = pass

    private def appendParameter[I](paramsAcc: Directive0, param: QueryParameter[I])(implicit ev: QueryParamConverter[I]): Directive0 = {
      (paramsAcc & ev.convert(param)).tmap(_ => ())
    }

    private def onlyQueryParams[I]: Parameter[I] => Seq[QueryParameter[I]] = {
      case q: QueryParameter[I] => List(q)
      case _ => Nil
    }
  }
}
