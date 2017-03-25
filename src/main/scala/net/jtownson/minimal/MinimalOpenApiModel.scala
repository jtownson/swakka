package net.jtownson.minimal

import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.HttpMethod

// Model supporting a single endpoint with query params.
// Good enough to verify principles in the code.
object MinimalOpenApiModel {

  sealed trait Parameter

  case class QueryParameter(name: Symbol)

  // in the real swagger model an operation can have multiple
  // response types (one type for a 200, another for a 500, etc).
  // could do this as a Tuple, Tn or a Seq. Tuple is probably good here.
  case class ResponseValue[T](responseCode: Int)

  case class Operation[T](parameters: Seq[Parameter] = Nil,
                       response: ResponseValue[T],
                       endpointImplementation: Map[Symbol, String] => ToResponseMarshallable)

  case class PathItem[T](method: HttpMethod, operation: Operation[T])

  case class OpenApiModel[T](path: String, pathItem: PathItem[T])
}

