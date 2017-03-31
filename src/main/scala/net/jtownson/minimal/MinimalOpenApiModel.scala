package net.jtownson.minimal

import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.{HttpMethod, HttpRequest}
import shapeless.{HList, HNil}

// Model supporting a single endpoint with query params.
// Good enough to verify principles in the code.
object MinimalOpenApiModel {

  case class QueryParameter[T](name: Symbol)

  // in the real swagger model an operation can have multiple
  // response types (one type for a 200, another for a 500, etc).
  // could do this as a Tuple, Tn or a Seq. Tuple is probably good here.
  case class ResponseValue[T](responseCode: Int)

  case class Operation[Params: ConvertibleToDirective0, T](parameters: Params = HNil,
                                                           response: ResponseValue[T],
                                                           endpointImplementation: HttpRequest => ToResponseMarshallable)

  case class PathItem[Params: ConvertibleToDirective0, T](method: HttpMethod, operation: Operation[Params, T])

  case class OpenApiModel[Params: ConvertibleToDirective0, T](path: String, pathItem: PathItem[Params, T])
}

