package net.jtownson.swakka

import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.{HttpMethod, HttpRequest}
import shapeless.{HList, HNil}

object OpenApiModel {

  case class QueryParameter[T](name: Symbol)
  case class PathParameter[T](name: Symbol)
  case class BodyParameter[T](name: Symbol)

  case class ResponseValue[T](responseCode: Int)

  case class Operation[Params <: HList : ConvertibleToDirective0, Responses <: HList](
    parameters: Params = HNil,
    responses: Responses = HNil,
    endpointImplementation: HttpRequest => ToResponseMarshallable)

  case class PathItem[Params <: HList : ConvertibleToDirective0, Responses <: HList](
    method: HttpMethod,
    operation: Operation[Params, Responses])

  case class Endpoint[Params <: HList : ConvertibleToDirective0, Responses <: HList](
    path: String,
    pathItem: PathItem[Params, Responses])

  case class OpenApi[Endpoints](endpoints: Endpoints)
}

