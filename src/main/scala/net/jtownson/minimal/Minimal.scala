package net.jtownson.minimal

import akka.http.scaladsl.model.HttpMethod
import akka.http.scaladsl.model.HttpMethods.GET
import akka.http.scaladsl.server._

object MinimalOpenApiModel {

  sealed trait Parameter

  case class QueryParameter(name: Symbol)

  case class Operation(parameters: Seq[Parameter] = Nil)
}

object RouteGen {

  import MinimalOpenApiModel._
  import akka.http.scaladsl.server.Directives._

  def routeGen(p: (String, (HttpMethod, Operation))): Directive1[Map[Symbol, String]] = {

    val (requestPath, requestMethod, operation) = (p._1, p._2._1, p._2._2)

    httpMethod(requestMethod) & path(requestPath) & QueryStringHandling2.parameters(operation)
  }

  private val httpMethod = Map(GET -> get)


  object QueryStringHandling {

    private val pNil: Directive1[List[String]] = pass.tmap(_ => List[String]())

    private val listConcat: ((List[String], List[String])) => List[String] = tl => tl._1 ::: tl._2

    private val appendParameter: (Directive1[List[String]], Directive1[String]) => Directive1[List[String]] =

      (ps, p) => {

        val pl1: Directive1[List[String]] = p.map[List[String]](List(_))

        (pl1 & ps).tmap(listConcat)
      }

    def parameters(o: Operation): Directive1[List[String]] = {
      val queryParams: Seq[QueryParameter] = o.parameters.flatMap {
        case q: QueryParameter => Some(q)
        case _ => None
      }

      val paramsMapped: Seq[Directive1[String]] = queryParams.map(param => parameter(param.name))

      paramsMapped.foldLeft(pNil)(appendParameter)
    }
  }

  object QueryStringHandling2 {
    private val pNil: Directive1[Map[Symbol, String]] = pass.tmap(_ => Map[Symbol, String]())

    private val mapAppend: ((Map[Symbol, String], Map[Symbol, String])) => Map[Symbol, String] = tl => tl._1 ++ tl._2

    private val appendParameter: (Directive1[Map[Symbol, String]], QueryParameter) => Directive1[Map[Symbol, String]] =

      (qps, qp) => {

        val pl1: Directive[Tuple1[Map[Symbol, String]]] = {
          val p: Directive1[String] = parameter(qp.name)
          val pl1: Directive1[Map[Symbol, String]] = p.map[Map[Symbol, String]](s => Map[Symbol, String](qp.name -> s))

          (pl1 & qps).tmap(mapAppend)
        }
      }

    private val onlyQueryParams = {
      case q: QueryParameter => Some(q)
      case _ => None
    }

    def parameters(o: Operation): Directive1[Map[Symbol, String]] =
      o.parameters.flatMap(onlyQueryParams).foldLeft(pNil)(appendParameter)

  }
}
