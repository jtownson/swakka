package net.jtownson.minimal

import akka.http.scaladsl.model.HttpMethod
import net.jtownson.minimal.MinimalOpenApiModel._
import spray.json.{DefaultJsonProtocol, JsFalse, JsNull, JsObject, JsString, JsonFormat, JsonWriter, RootJsonFormat, RootJsonWriter}

class MinimalJsonProtocol[T](implicit ev: SchemaWriter[T]) extends DefaultJsonProtocol {

  private def asString(method: HttpMethod): String = method match {
    case HttpMethod(value, _, _, _) => value.toLowerCase
  }


  val parameterWriter: JsonWriter[QueryParameter[_]] = {
    case qp: QueryParameter[_] =>
      JsObject(
        "name" -> JsString(qp.name),
        "in" -> JsString("query"),
        "description" -> JsNull,
        "required" -> JsFalse,
        "type" -> JsString("string")
      )
  }

  implicit val parameterFormat: JsonFormat[QueryParameter[_]] = lift(parameterWriter)

  def operationWriter: JsonWriter[Operation[_, T]] = (operation: Operation[_, T]) =>
    operation.parameters match {
      case Nil => JsObject(
        "responses" -> responseValueWriter.write(operation.response)
      )
      case _ => JsObject(
//        "parameters" -> seqFormat[QueryParameter[_]].write(operation.parameters), // TODO
        "responses" -> responseValueWriter.write(operation.response)
      )
    }

  implicit val operationFormat: JsonFormat[Operation[_, T]] = lift(operationWriter)

  private val jsonSchemaJsonWriter = new MinimalJsonSchemaJsonProtocol[T].jsonSchemaJsonWriter

  val responseValueWriter: JsonWriter[ResponseValue[T]] = (responseValue: ResponseValue[T]) =>
    JsObject(
      String.valueOf(responseValue.responseCode) -> JsObject(
        "schema" -> jsonSchemaJsonWriter.write(MinimalJsonSchema[T]())
      )
    )

  implicit def responseValueFormat: JsonFormat[ResponseValue[T]] = lift(responseValueWriter)

  implicit val queryParameterWriter: RootJsonFormat[QueryParameter[_]] = jsonFormat1(QueryParameter[String]) // TODO

  val pathItemWriter: JsonWriter[PathItem[_, T]] = (pathItem: PathItem[_, T]) =>
    JsObject(
      asString(pathItem.method) -> operationWriter.write(pathItem.operation)
    )

  implicit val pathItemFormat: JsonFormat[PathItem[_, T]] = lift(pathItemWriter)


  val openApiModelWriter: RootJsonWriter[OpenApiModel[_, T]] = (openApiModel: OpenApiModel[_, T]) =>
    JsObject(
      openApiModel.path -> pathItemWriter.write(openApiModel.pathItem)
    )

  implicit val openApiModelFormat: RootJsonFormat[OpenApiModel[_, T]] = lift(openApiModelWriter)
}
