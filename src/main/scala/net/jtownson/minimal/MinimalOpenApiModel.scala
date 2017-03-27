package net.jtownson.minimal

import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.{HttpMethod, HttpRequest}

// Model supporting a single endpoint with query params.
// Good enough to verify principles in the code.
object MinimalOpenApiModel {

  sealed trait Parameter[+T]

  case class QueryParameter[T](name: Symbol) extends Parameter[T]

  // in the real swagger model an operation can have multiple
  // response types (one type for a 200, another for a 500, etc).
  // could do this as a Tuple, Tn or a Seq. Tuple is probably good here.
  case class ResponseValue[T](responseCode: Int)

  case class Operation[I, T](parameters: Seq[Parameter[I]] = Nil,
                       response: ResponseValue[T],
                       endpointImplementation: HttpRequest => ToResponseMarshallable)

  case class PathItem[I, T](method: HttpMethod, operation: Operation[I, T])

  case class OpenApiModel[I, T](path: String, pathItem: PathItem[I, T])
}

