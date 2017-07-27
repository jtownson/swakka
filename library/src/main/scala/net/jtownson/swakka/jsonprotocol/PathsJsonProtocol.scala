package net.jtownson.swakka.jsonprotocol

import akka.http.scaladsl.model.HttpMethod
import net.jtownson.swakka.OpenApiModel._
import net.jtownson.swakka.jsonprotocol.Flattener.flattenToObject
import net.jtownson.swakka.jsonprotocol.PathsJsonFormat.func2Format
import net.jtownson.swakka.model.{Contact, Info, License}
import shapeless.{::, HList, HNil}
import spray.json.{DefaultJsonProtocol, JsArray, JsObject, JsString, JsValue, JsonWriter, RootJsonFormat, RootJsonWriter}


// A JsonProtocol supporting OpenApi paths
trait PathsJsonProtocol extends DefaultJsonProtocol {

  private def operationWriter[Params <: HList, Responses]
  (implicit ev1: ParameterJsonFormat[Params], ev2: ResponseJsonFormat[Responses]): JsonWriter[Operation[Params, Responses]] =
    (operation: Operation[Params, Responses]) => {

      val parameters: JsValue = ev1.write(operation.parameters)
      val responses = ev2.write(operation.responses)

      val tags: Option[JsArray] = operation.tags.
        map(tags => tags.map(JsString(_))).
        map(_.toList).
        map(JsArray(_: _*))

      val fields: Seq[(String, JsValue)] = List(
        operation.summary.map("summary" -> JsString(_)),
        operation.operationId.map("operationId" -> JsString(_)),
        tags.map("tags" -> _),
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

  private def asString(method: HttpMethod): String = method match {
    case HttpMethod(value, _, _, _) => value.toLowerCase
  }

  private def asJsArray(key: String, entries: Option[Seq[_]]): Option[(String, JsValue)] = {
    entries.map(s => key -> JsArray(s.map(ss => JsString(ss.toString)): _*))
  }

  private val contactWriter: JsonWriter[Contact] = (contact: Contact) => {
    val fields: List[(String, JsValue)] = List(
      contact.name.map("name" -> JsString(_)),
      contact.url.map("url" -> JsString(_)),
      contact.email.map("email" -> JsString(_))
    ).flatten

    JsObject(fields: _*)
  }

  private val licenceWriter: JsonWriter[License] = (licence: License) => {
    val fields: List[(String, JsValue)] = List(
      Some("name" -> JsString(licence.name)),
      licence.url.map("url" -> JsString(_))).flatten

    JsObject(fields: _*)
  }

  private val infoWriter: JsonWriter[Info] = (info: Info) => {

    val fields: List[(String, JsValue)] = List(
      Some("title" -> JsString(info.title)),
      Some("version" -> JsString(info.version)),
      info.description.map("description" -> JsString(_)),
      info.termsOfService.map("termsOfService" -> JsString(_)),
      info.contact.map("contact" -> contactWriter.write(_)),
      info.licence.map("license" -> licenceWriter.write(_))).
      flatten

    JsObject(fields: _*)
  }


  implicit val hNilFormat: PathsJsonFormat[HNil] =
    _ => JsObject()

  implicit def hConsFormat[H, T <: HList]
  (implicit hFmt: PathsJsonFormat[H], tFmt: PathsJsonFormat[T]):
  PathsJsonFormat[H :: T] =
    func2Format((l: H :: T) => flattenToObject(JsArray(hFmt.write(l.head), tFmt.write(l.tail))))


  implicit def singlePathItemFormat[Params <: HList, Responses]
  (implicit ev1: ParameterJsonFormat[Params], ev2: ResponseJsonFormat[Responses]):
  PathsJsonFormat[PathItem[Params, Responses]] =
    func2Format((pathItem: PathItem[Params, Responses]) => JsObject(
      pathItem.path -> JsObject(
        asString(pathItem.method) -> operationWriter[Params, Responses].write(pathItem.operation)
      )
    ))


  implicit def apiWriter[Paths, SecurityDefinitions]
  (implicit ev: PathsJsonFormat[Paths]): RootJsonWriter[OpenApi[Paths, SecurityDefinitions]] =
    new RootJsonWriter[OpenApi[Paths, SecurityDefinitions]] {
      override def write(api: OpenApi[Paths, SecurityDefinitions]): JsValue = {

        val paths = ev.write(api.paths)

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

  implicit def apiFormat[Paths, SecurityDefinitions]
  (implicit ev: PathsJsonFormat[Paths]): RootJsonFormat[OpenApi[Paths, SecurityDefinitions]] =
    lift(apiWriter[Paths, SecurityDefinitions])
}

object PathsJsonProtocol extends PathsJsonProtocol