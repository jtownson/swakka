package net.jtownson.minimal

import akka.http.scaladsl.model.HttpMethod
import net.jtownson.minimal.MinimalOpenApiModel._
import shapeless.HList
import spray.json.{DefaultJsonProtocol, JsObject, JsonFormat, JsonWriter, RootJsonFormat, RootJsonWriter}

class MinimalJsonProtocol[Params <: HList, Responses <: HList](

  implicit ev1: ParameterJsonFormat[Params], ev2: ResponseJsonFormat[Responses])

  extends DefaultJsonProtocol {


  private def asString(method: HttpMethod): String = method match {
    case HttpMethod(value, _, _, _) => value.toLowerCase
  }


  val operationWriter: JsonWriter[Operation[Params, Responses]] = (operation: Operation[Params, Responses]) => {
    val parameters: HList = operation.parameters

    if (parameters.productArity == 0)
      JsObject(
        "responses" -> ev2.write(operation.responses))
    else
      JsObject(
        "parameters" -> ev1.write(operation.parameters),
        "responses" -> ev2.write(operation.responses))
  }

  implicit val operationFormat: JsonFormat[Operation[Params, Responses]] = lift(operationWriter)

  val pathItemWriter: JsonWriter[PathItem[Params, Responses]] = (pathItem: PathItem[Params, Responses]) =>
    JsObject(
      asString(pathItem.method) -> operationWriter.write(pathItem.operation)
    )

  implicit val pathItemFormat: JsonFormat[PathItem[Params, Responses]] = lift(pathItemWriter)


  val openApiModelWriter: RootJsonWriter[OpenApiModel[Params, Responses]] = (openApiModel: OpenApiModel[Params, Responses]) =>
    JsObject(
      openApiModel.path -> pathItemWriter.write(openApiModel.pathItem)
    )

  implicit val openApiModelFormat: RootJsonFormat[OpenApiModel[Params, Responses]] = lift(openApiModelWriter)
}
