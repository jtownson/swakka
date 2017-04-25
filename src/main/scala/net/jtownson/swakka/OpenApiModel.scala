package net.jtownson.swakka

import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.{HttpMethod, HttpRequest}
import net.jtownson.swakka.model.Info
import net.jtownson.swakka.model.ModelDefaults._
import net.jtownson.swakka.routegen.ConvertibleToDirective0
import shapeless.{HList, HNil}

object OpenApiModel {

  case class QueryParameter[T](name: Symbol)
  case class PathParameter[T](name: Symbol)
  case class BodyParameter[T](name: Symbol)

  case class ResponseValue[T](responseCode: Int)

  case class Operation[Params <: HList : ConvertibleToDirective0, Responses](
    summary: Option[String] = None,
    operationId: Option[String] = None,
    parameters: Params = HNil,
    responses: Responses = HNil,
    endpointImplementation: HttpRequest => ToResponseMarshallable)

  case class PathItem[Params <: HList : ConvertibleToDirective0, Responses](
    path: String,
    method: HttpMethod,
    operation: Operation[Params, Responses])

  case class OpenApi[Paths](
    info: Info = pointlessInfo,
    host: Option[String] = None,
    basePath: Option[String] = None,
    schemes: Option[Seq[String]] = None,
    consumes: Option[Seq[String]] = None,
    produces: Option[Seq[String]] = None,
    paths: Paths)
}

