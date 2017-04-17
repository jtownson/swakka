package net.jtownson.swakka

import akka.http.scaladsl.model.HttpMethod
import net.jtownson.swakka.EndpointJsonFormat.func2Format
import net.jtownson.swakka.Flattener.flattenToObject
import net.jtownson.swakka.OpenApiModel._
import shapeless.{::, HList, HNil}
import spray.json.{DefaultJsonProtocol, JsArray, JsNull, JsObject, JsValue, JsonFormat, JsonWriter, RootJsonFormat, RootJsonWriter}

// A JsonProtocol supporting the OpenApiModel
trait EndpointsJsonProtocol extends DefaultJsonProtocol {

  def operationWriter[Params <: HList, Responses <: HList]
  (implicit ev1: ParameterJsonFormat[Params], ev2: ResponseJsonFormat[Responses]): JsonWriter[Operation[Params, Responses]] =
    (operation: Operation[Params, Responses]) => {

      val parameters: HList = operation.parameters

      if (parameters.productArity == 0)
        JsObject(
          "responses" -> ev2.write(operation.responses))
      else
        JsObject(
          "parameters" -> ev1.write(operation.parameters),
          "responses" -> ev2.write(operation.responses))
    }

  implicit def operationFormat[Params <: HList, Responses <: HList]
  (implicit ev1: ParameterJsonFormat[Params], ev2: ResponseJsonFormat[Responses]): JsonFormat[Operation[Params, Responses]] =
    lift(operationWriter[Params, Responses])

  def pathItemWriter[Params <: HList, Responses <: HList]
  (implicit ev1: ParameterJsonFormat[Params], ev2: ResponseJsonFormat[Responses]): JsonWriter[PathItem[Params, Responses]] =
    (pathItem: PathItem[Params, Responses]) =>
      JsObject(
        asString(pathItem.method) -> operationWriter[Params, Responses].write(pathItem.operation)
      )

  private def asString(method: HttpMethod): String = method match {
    case HttpMethod(value, _, _, _) => value.toLowerCase
  }

  implicit def pathItemFormat[Params <: HList, Responses <: HList]
  (implicit ev1: ParameterJsonFormat[Params], ev2: ResponseJsonFormat[Responses]): JsonFormat[PathItem[Params, Responses]] =
    lift(pathItemWriter[Params, Responses])

  implicit val hNilFormat: EndpointJsonFormat[HNil] =
    _ => JsNull

  implicit def hConsFormat[H, T <: HList]
  (implicit hFmt: EndpointJsonFormat[H], tFmt: EndpointJsonFormat[T]):
  EndpointJsonFormat[H :: T] =
    func2Format((l: H :: T) => flattenToObject(JsArray(hFmt.write(l.head), tFmt.write(l.tail))))

  implicit def singleEndpointFormat[Params <: HList, Responses <: HList]
  (implicit ev1: ParameterJsonFormat[Params], ev2: ResponseJsonFormat[Responses]):
  EndpointJsonFormat[Endpoint[Params, Responses]] =
    func2Format((endpoint: Endpoint[Params, Responses]) => JsObject(
      endpoint.path -> pathItemWriter.write(endpoint.pathItem)
    ))

  def apiWriter[Endpoints](implicit ev: EndpointJsonFormat[Endpoints]): RootJsonWriter[OpenApi[Endpoints]] =
    new RootJsonWriter[OpenApi[Endpoints]] {
      override def write(api: OpenApi[Endpoints]): JsValue =
        JsObject(
          "paths" -> ev.write(api.endpoints)
        )
    }

  def apiFormat[Endpoints](implicit ev: EndpointJsonFormat[Endpoints]): RootJsonFormat[OpenApi[Endpoints]] =
    lift(apiWriter[Endpoints])
}

object EndpointsJsonProtocol extends EndpointsJsonProtocol