/*
 * Copyright 2017 Jeremy Townson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.jtownson.swakka.jsonprotocol

import akka.http.scaladsl.model.HttpMethod
import net.jtownson.swakka.OpenApiModel._
import net.jtownson.swakka.jsonprotocol.Flattener.flattenToObject
import net.jtownson.swakka.jsonprotocol.PathsJsonFormat.func2Format
import net.jtownson.swakka.model.{Contact, Info, License}
import net.jtownson.swakka.jsonprotocol.SecurityDefinitionsJsonProtocol.securityRequirementJsonFormat
import shapeless.{::, HList, HNil}
import spray.json._
import spray.json.{DefaultJsonProtocol, JsArray, JsObject, JsString, JsValue, JsonWriter, RootJsonFormat, RootJsonWriter}

// A JsonProtocol supporting OpenApi paths
trait PathsJsonProtocol extends DefaultJsonProtocol {

  private def operationWriter[F, Params <: HList, Responses]
  (implicit ev1: ParameterJsonFormat[Params], ev2: ResponseJsonFormat[Responses]): JsonWriter[Operation[F, Params, Responses]] =
    (operation: Operation[F, Params, Responses]) => {

      val parameters: JsValue = ev1.write(operation.parameters)
      val responses = ev2.write(operation.responses)

      val fields: Seq[(String, JsValue)] = List(
        operation.summary.map("summary" -> JsString(_)),
        operation.operationId.map("operationId" -> JsString(_)),
        optionalArrayField("tags", operation.tags),
        optionalArrayField("consumes", operation.consumes),
        optionalArrayField("produces", operation.produces),
        operation.description.map("description" -> JsString(_)),
        optionalArrayField("parameters", parameters),
        optionalObjectField("responses", responses),
        operation.security.map("security" -> _.toJson)
      ).flatten

      JsObject(fields: _*)
    }

  private def optionalArrayField(s: String, f: Option[Seq[String]]): Option[(String, JsArray)] =
    f.map(tags => tags.map(JsString(_))).
      map(_.toList).
      map(JsArray(_: _*)).map(s -> _)

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


  implicit def singlePathItemFormat[F, Params <: HList, Responses]
  (implicit ev1: ParameterJsonFormat[Params], ev2: ResponseJsonFormat[Responses]):
  PathsJsonFormat[PathItem[F, Params, Responses]] =
    func2Format((pathItem: PathItem[F, Params, Responses]) => JsObject(
      pathItem.path -> JsObject(
        asString(pathItem.method) -> operationWriter[F, Params, Responses].write(pathItem.operation)
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