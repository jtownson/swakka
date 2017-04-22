package net.jtownson.swakka.jsonprotocol

import akka.http.scaladsl.model.HttpMethod
import net.jtownson.swakka.OpenApiModel._
import net.jtownson.swakka.jsonprotocol.EndpointJsonFormat.func2Format
import net.jtownson.swakka.jsonprotocol.Flattener.flattenToObject
import net.jtownson.swakka.model.{Contact, Info, Licence}
import shapeless.{::, HList, HNil}
import spray.json.{DefaultJsonProtocol, JsArray, JsObject, JsString, JsValue, JsonFormat, JsonWriter, RootJsonFormat, RootJsonWriter}


// A JsonProtocol supporting OpenApi endpoints
trait EndpointsJsonProtocol extends DefaultJsonProtocol {

  def operationWriter[Params <: HList, Responses]
  (implicit ev1: ParameterJsonFormat[Params], ev2: ResponseJsonFormat[Responses]): JsonWriter[Operation[Params, Responses]] =
    (operation: Operation[Params, Responses]) => {

      val parameters: JsValue = ev1.write(operation.parameters)
      val responses = ev2.write(operation.responses)

      val fields: Seq[(String, JsValue)] = List(
        optionalArrayField("parameters", parameters),
        optionalObjectField("responses", responses)).
        flatten

      JsObject(fields: _*)
    }

  private def optionalArrayField(s: String, j: JsValue): Option[(String, JsValue)] = j match {
    case (JsArray(elements)) =>
      optionalField(s, j, elements)
    case _ =>
      None
  }


  private def optionalObjectField(s: String, j: JsValue): Option[(String, JsValue)] = j match {
    case (JsObject(fields)) =>
      optionalField(s, j, fields)
    case _ =>
      None
  }

  private def optionalField(s: String, j: JsValue, elements: Iterable[_]) = {
    if (elements.isEmpty)
      None
    else
      Some((s, j))
  }

  implicit def operationFormat[Params <: HList, Responses]
  (implicit ev1: ParameterJsonFormat[Params], ev2: ResponseJsonFormat[Responses]): JsonFormat[Operation[Params, Responses]] =
    lift(operationWriter[Params, Responses])

  def pathItemWriter[Params <: HList, Responses]
  (implicit ev1: ParameterJsonFormat[Params], ev2: ResponseJsonFormat[Responses]): JsonWriter[PathItem[Params, Responses]] =
    (pathItem: PathItem[Params, Responses]) =>
      JsObject(
        asString(pathItem.method) -> operationWriter[Params, Responses].write(pathItem.operation)
      )

  private def asString(method: HttpMethod): String = method match {
    case HttpMethod(value, _, _, _) => value.toLowerCase
  }

  private def asJsArray(key: String, entries: Option[Seq[_]]): Option[(String, JsValue)] = {
    entries.map(s => key -> JsArray(s.map(ss => JsString(ss.toString)): _*))
  }

  implicit def pathItemFormat[Params <: HList, Responses]
  (implicit ev1: ParameterJsonFormat[Params], ev2: ResponseJsonFormat[Responses]): JsonFormat[PathItem[Params, Responses]] =
    lift(pathItemWriter[Params, Responses])

  implicit val hNilFormat: EndpointJsonFormat[HNil] =
    _ => JsObject()

  implicit def hConsFormat[H, T <: HList]
  (implicit hFmt: EndpointJsonFormat[H], tFmt: EndpointJsonFormat[T]):
  EndpointJsonFormat[H :: T] =
    func2Format((l: H :: T) => flattenToObject(JsArray(hFmt.write(l.head), tFmt.write(l.tail))))

  implicit def singleEndpointFormat[Params <: HList, Responses]
  (implicit ev1: ParameterJsonFormat[Params], ev2: ResponseJsonFormat[Responses]):
  EndpointJsonFormat[Endpoint[Params, Responses]] =
    func2Format((endpoint: Endpoint[Params, Responses]) => JsObject(
      endpoint.path -> pathItemWriter.write(endpoint.pathItem)
    ))

  val contactWriter: JsonWriter[Contact] = (contact: Contact) => {
    val fields: List[(String, JsValue)] = List(
      contact.name.map("name" -> JsString(_)),
      contact.url.map("url" -> JsString(_)),
      contact.email.map("email" -> JsString(_))
    ).flatten

    JsObject(fields: _*)
  }

  val licenceWriter: JsonWriter[Licence] = (licence: Licence) => {
    val fields: List[(String, JsValue)] = List(
      Some("name" -> JsString(licence.name)),
      licence.url.map("url" -> JsString(_))).flatten

    JsObject(fields: _*)
  }

  val infoWriter: JsonWriter[Info] = (info: Info) => {

    val fields: List[(String, JsValue)] = List(
      Some("title" -> JsString(info.title)),
      Some("version" -> JsString(info.version)),
      info.description.map("description" -> JsString(_)),
      info.termsOfService.map("termsOfService" -> JsString(_)),
      info.contact.map("contact" -> contactWriter.write(_)),
      info.licence.map("licence" -> licenceWriter.write(_))).
      flatten

    JsObject(fields: _*)
  }

  def apiWriter[Endpoints](implicit ev: EndpointJsonFormat[Endpoints]): RootJsonWriter[OpenApi[Endpoints]] =
    new RootJsonWriter[OpenApi[Endpoints]] {
      override def write(api: OpenApi[Endpoints]): JsValue = {

        val paths = ev.write(api.endpoints)

        val fields = List(
          Some("swagger" -> JsString("2.0")),
          Some("info" -> infoWriter.write(api.info)),
          api.host.map("host" -> JsString(_)),
          api.basePath.map("basePath" -> JsString(_)),
          asJsArray("schemes", api.schemes),
          asJsArray("consumes", api.consumes),
          asJsArray("produces", api.produces),
          Some("paths" -> paths)).flatten

        JsObject(fields: _*)
      }
    }

  def apiFormat[Endpoints](implicit ev: EndpointJsonFormat[Endpoints]): RootJsonFormat[OpenApi[Endpoints]] =
    lift(apiWriter[Endpoints])
}

object EndpointsJsonProtocol extends EndpointsJsonProtocol