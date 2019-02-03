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

package net.jtownson.swakka.openapijson

import akka.http.scaladsl.model.HttpMethod
import net.jtownson.swakka.openapimodel._
import net.jtownson.swakka.openapijson.Flattener.flattenToObject
import net.jtownson.swakka.openapijson.PathsJsonFormat.instance
import shapeless.{::, Generic, HList, HNil, |¬|}
import spray.json._
import spray.json.{JsArray, JsObject, JsString, JsValue, JsonWriter}

// A JsonProtocol supporting OpenApi paths
trait PathsJsonProtocol {

  private def operationWriter[Params <: Product, EndpointFunction, Responses](
      implicit ev1: ParameterJsonFormat[Params],
      ev2: ResponseJsonFormat[Responses])
    : JsonWriter[Operation[Params, EndpointFunction, Responses]] =
    JsonWriter.func2Writer((operation: Operation[Params, EndpointFunction, Responses]) => {

      val parameters: JsValue = ev1.write(operation.parameters)
      val responses = ev2.write(operation.responses)

      val fields: Seq[(String, JsValue)] = List(
        operation.summary.map("summary" -> JsString(_)),
        operation.operationId.map("operationId" -> JsString(_)),
        optionalArrayField("tags", operation.tags),
        optionalArrayField("consumes", operation.consumes),
        optionalArrayField("produces", operation.produces),
        operation.description.map("description" -> JsString(_)),
        optionalBooleanField("deprecated", operation.deprecated),
        optionalArrayField("parameters", parameters),
        optionalObjectField("responses", responses),
        operation.security.map("security" -> _.toJson)
      ).flatten

      JsObject(fields: _*)
    })

  private def optionalBooleanField(s: String, b: Boolean): Option[(String, JsBoolean)] =
    Some(s -> JsBoolean(b)).filter(_ => b)

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
      case JsArray(elements) =>
        optionalField(s, j, elements)
      case _ =>
        None
    }

  private def optionalObjectField(s: String,
                                  j: JsValue): Option[(String, JsValue)] =
    j match {
      case JsObject(fields) =>
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
    instance(_ => JsObject())

  implicit def hConsPathItemFormat[H, T <: HList](
      implicit hFmt: PathsJsonFormat[H],
      tFmt: PathsJsonFormat[T]): PathsJsonFormat[H :: T] =
    instance((l: H :: T) =>
      flattenToObject(JsArray(hFmt.write(l.head), tFmt.write(l.tail))))

  // Because PathItem is a Product, with a Generic.Aux, the compiler occasionally
  // (and apparently non-deterministically) goes down the wrong route and applies
  // this def instead of singlePathItemFormat.
  // Use shapeless's |¬| to force use of the more specific JsonFormat provided by singlePathItemFormat.
  implicit def genericPathItemFormat[Paths: |¬|[PathItem[_, _, _]]#λ, PathsList]
  (implicit gen: Generic.Aux[Paths, PathsList],
   ev: PathsJsonFormat[PathsList]): PathsJsonFormat[Paths] =
    instance(paths => ev.write(gen.to(paths)))

  implicit def singlePathItemFormat[Params <: Product,
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
