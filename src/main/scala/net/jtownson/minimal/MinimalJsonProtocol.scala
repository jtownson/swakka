package net.jtownson.minimal

import akka.http.scaladsl.model.HttpMethod
import net.jtownson.minimal.MinimalOpenApiModel._
import shapeless.HList
import spray.json.{DefaultJsonProtocol, JsObject, JsonFormat, JsonWriter, RootJsonFormat, RootJsonWriter}

class MinimalJsonProtocol[Params <: HList, T](implicit ev1: JsonWriter[Params], ev2: SchemaWriter[T])
  extends DefaultJsonProtocol {

  private def asString(method: HttpMethod): String = method match {
    case HttpMethod(value, _, _, _) => value.toLowerCase
  }

  val operationWriter: JsonWriter[Operation[Params, T]] = (operation: Operation[Params, T]) => {
    val parameters: HList = operation.parameters

    if (parameters.productArity == 0)
      JsObject(
        "responses" -> responseValueWriter.write(operation.response))
    else
      JsObject(
        "parameters" -> ev1.write(operation.parameters),
        "responses" -> responseValueWriter.write(operation.response))
  }

  implicit val operationFormat: JsonFormat[Operation[Params, T]] = lift(operationWriter)

  private val jsonSchemaJsonWriter = new MinimalJsonSchemaJsonProtocol[T].jsonSchemaJsonWriter

  val responseValueWriter: JsonWriter[ResponseValue[T]] = (responseValue: ResponseValue[T]) =>
    JsObject(
      String.valueOf(responseValue.responseCode) -> JsObject(
        "schema" -> jsonSchemaJsonWriter.write(MinimalJsonSchema[T]())
      )
    )

  implicit val responseValueFormat: JsonFormat[ResponseValue[T]] = lift(responseValueWriter)

  val pathItemWriter: JsonWriter[PathItem[Params, T]] = (pathItem: PathItem[Params, T]) =>
    JsObject(
      asString(pathItem.method) -> operationWriter.write(pathItem.operation)
    )

  implicit val pathItemFormat: JsonFormat[PathItem[Params, T]] = lift(pathItemWriter)


  val openApiModelWriter: RootJsonWriter[OpenApiModel[Params, T]] = (openApiModel: OpenApiModel[Params, T]) =>
    JsObject(
      openApiModel.path -> pathItemWriter.write(openApiModel.pathItem)
    )

  implicit val openApiModelFormat: RootJsonFormat[OpenApiModel[Params, T]] = lift(openApiModelWriter)
}
