package net.jtownson.minimal

import akka.http.scaladsl.model.HttpMethod
import net.jtownson.minimal.MinimalOpenApiModel._
import spray.json.{DefaultJsonProtocol, JsFalse, JsNull, JsObject, JsString, JsonFormat, JsonWriter, RootJsonFormat, RootJsonWriter}

class MinimalJsonProtocol[T](implicit ev: SchemaWriter[T]) extends DefaultJsonProtocol {

  private def asString(method: HttpMethod): String = method match {
    case HttpMethod(value, _, _, _) => value.toLowerCase
  }

  val parameterWriter: JsonWriter[Parameter] = {
    case qp: QueryParameter =>
      JsObject(
        "name" -> JsString(qp.name),
        "in" -> JsString("query"),
        "description" -> JsNull,
        "required" -> JsFalse,
        "type" -> JsString("string")
      )
  }

  implicit val parameterFormat: JsonFormat[Parameter] = lift(parameterWriter)

  def operationWriter: JsonWriter[Operation[T]] = (operation: Operation[T]) =>
    operation.parameters match {
      case Nil => JsObject(
        "responses" -> responseValueWriter.write(operation.response)
      )
      case _ => JsObject(
        "parameters" -> seqFormat[Parameter].write(operation.parameters),
        "responses" -> responseValueWriter.write(operation.response)
      )
    }

  implicit val operationFormat: JsonFormat[Operation[T]] = lift(operationWriter)

  import MinimalJsonSchemaJsonProtocol._
  private val jsonSchemaJsonWriter = new MinimalJsonSchemaJsonProtocol[T].jsonSchemaJsonWriter

  val responseValueWriter: JsonWriter[ResponseValue[T]] = (responseValue: ResponseValue[T]) =>
    JsObject(
      String.valueOf(responseValue.responseCode) -> JsObject(
        "schema" -> jsonSchemaJsonWriter.write(MinimalJsonSchema[T]())
      )
    )

  implicit def responseValueFormat: JsonFormat[ResponseValue[T]] = lift(responseValueWriter)

  implicit val queryParameterWriter: RootJsonFormat[QueryParameter] = jsonFormat1(QueryParameter) // TODO

  val pathItemWriter: JsonWriter[PathItem[T]] = (pathItem: PathItem[T]) =>
    JsObject(
      asString(pathItem.method) -> operationWriter.write(pathItem.operation)
    )

  implicit val pathItemFormat: JsonFormat[PathItem[T]] = lift(pathItemWriter)


  val openApiModelWriter: RootJsonWriter[OpenApiModel[T]] = (openApiModel: OpenApiModel[T]) =>
    JsObject(
      openApiModel.path -> pathItemWriter.write(openApiModel.pathItem)
    )

  implicit val openApiModelFormat: RootJsonFormat[OpenApiModel[T]] = lift(openApiModelWriter)
}
