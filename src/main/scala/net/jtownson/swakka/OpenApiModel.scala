package net.jtownson.swakka

import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.{HttpMethod, HttpRequest}
import net.jtownson.swakka.routegen.ConvertibleToDirective0
import shapeless.{HList, HNil}

object OpenApiModel {

  case class QueryParameter[T](name: Symbol)
  case class PathParameter[T](name: Symbol)
  case class BodyParameter[T](name: Symbol)

  case class ResponseValue[T](responseCode: Int)

  case class Operation[Params: ConvertibleToDirective0, Responses](
    parameters: Params = HNil,
    responses: Responses = HNil,
    endpointImplementation: HttpRequest => ToResponseMarshallable)

  case class PathItem[Params: ConvertibleToDirective0, Responses](
    method: HttpMethod,
    operation: Operation[Params, Responses])

  case class Endpoint[Params: ConvertibleToDirective0, Responses](
    path: String,
    pathItem: PathItem[Params, Responses])

  case class OpenApi[Endpoints](endpoints: Endpoints)
}

