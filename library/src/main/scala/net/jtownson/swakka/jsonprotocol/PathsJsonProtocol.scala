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
import net.jtownson.swakka.jsonprotocol.PathsJsonFormat.instance
import net.jtownson.swakka.jsonprotocol.SecurityDefinitionsJsonProtocol.securityRequirementJsonFormat
import shapeless.{::, HList, HNil}
import spray.json._
import spray.json.{
  JsArray,
  JsObject,
  JsString,
  JsValue,
  JsonWriter
}

// A JsonProtocol supporting OpenApi paths
trait PathsJsonProtocol
    extends ParametersJsonProtocol
    with ResponsesJsonProtocol {

  private def operationWriter[Params <: HList, EndpointFunction, Responses](
      implicit ev1: ParameterJsonFormat[Params],
      ev2: ResponseJsonFormat[Responses])
    : JsonWriter[Operation[Params, EndpointFunction, Responses]] =
    (operation: Operation[Params, EndpointFunction, Responses]) => {

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

  private def optionalArrayField(
      s: String,
      f: Option[Seq[String]]): Option[(String, JsArray)] =
    f.map(tags => tags.map(JsString(_)))
      .map(_.toList)
      .map(JsArray(_: _*))
      .map(s -> _)

  private def optionalArrayField(s: String,
                                 j: JsValue): Option[(String, JsValue)] =
    j match {
      case (JsArray(elements)) =>
        optionalField(s, j, elements)
      case _ =>
        None
    }

  private def optionalObjectField(s: String,
                                  j: JsValue): Option[(String, JsValue)] =
    j match {
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

  implicit val hNilPathItemFormat: PathsJsonFormat[HNil] =
    _ => JsObject()

  implicit def hConsPathItemFormat[H, T <: HList](
      implicit hFmt: PathsJsonFormat[H],
      tFmt: PathsJsonFormat[T]): PathsJsonFormat[H :: T] =
    instance((l: H :: T) =>
      flattenToObject(JsArray(hFmt.write(l.head), tFmt.write(l.tail))))

  implicit def singlePathItemFormat[Params <: HList,
                                    EndpointFunction,
                                    Responses](
      implicit ev1: ParameterJsonFormat[Params],
      ev2: ResponseJsonFormat[Responses])
    : PathsJsonFormat[PathItem[Params, EndpointFunction, Responses]] =
    instance(
      (pathItem: PathItem[Params, EndpointFunction, Responses]) =>
        JsObject(
          pathItem.path -> JsObject(
            asString(pathItem.method) -> operationWriter[Params,
                                                         EndpointFunction,
                                                         Responses].write(
              pathItem.operation)
          )
      ))
}

object PathsJsonProtocol extends PathsJsonProtocol
