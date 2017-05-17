package net.jtownson.swakka

import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.{HttpMethod, HttpRequest}
import net.jtownson.swakka.model.Info
import net.jtownson.swakka.model.ModelDefaults._
import net.jtownson.swakka.routegen.ConvertibleToDirective
import shapeless.{HList, HNil}

object OpenApiModel {

  case class Header[T](name: Symbol, description: Option[String] = None)

  case class ResponseValue[T, Headers](responseCode: String, description: String, headers: Headers = HNil)

  case class Operation[Params <: HList : ConvertibleToDirective, Responses](
   summary: Option[String] = None,
   operationId: Option[String] = None,
   tags: Option[Seq[String]] = None,
   parameters: Params = HNil,
   responses: Responses = HNil,
   endpointImplementation: (Params, HttpRequest) => ToResponseMarshallable)

  case class PathItem[Params <: HList : ConvertibleToDirective, Responses](
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

