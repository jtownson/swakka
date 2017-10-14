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

import net.jtownson.swakka.jsonschema.{JsonSchema, SchemaWriter}
import shapeless.{::, HList, HNil}
import spray.json._
import ParameterJsonFormat.func2Format
import net.jtownson.swakka.misc.jsObject
import net.jtownson.swakka.model.Parameters._

import scala.reflect.runtime.universe.TypeTag

trait ParametersJsonProtocol extends MultiValuedJsonProtocol with FormFieldParametersJsonProtocol with DefaultJsonProtocol {

  implicit val strReqQueryParamFormat: ParameterJsonFormat[QueryParameter[String]] =
    (qp: QueryParameter[String]) => simpleParam(qp.name, "query", qp.description, true, "string", None, None, enumOf(qp))

  implicit val strOptQueryParamFormat: ParameterJsonFormat[QueryParameter[Option[String]]] =
    (qp: QueryParameter[Option[String]]) => simpleParam(qp.name, "query", qp.description, false, "string", None, defaultOf(qp), enumOfOption(qp))

  implicit val floatReqQueryParamFormat: ParameterJsonFormat[QueryParameter[Float]] =
    (qp: QueryParameter[Float]) => simpleParam(qp.name, "query", qp.description, true, "number", Some("float"), None, enumOf(qp))

  implicit val floatOptQueryParamFormat: ParameterJsonFormat[QueryParameter[Option[Float]]] =
    (qp: QueryParameter[Option[Float]]) => simpleParam(qp.name, "query", qp.description, false, "number", Some("float"), defaultOf(qp), enumOfOption(qp))

  implicit val doubleReqQueryParamFormat: ParameterJsonFormat[QueryParameter[Double]] =
    (qp: QueryParameter[Double]) => simpleParam(qp.name, "query", qp.description, true, "number", Some("double"), None, enumOf(qp))

  implicit val doubleOptQueryParamFormat: ParameterJsonFormat[QueryParameter[Option[Double]]] =
    (qp: QueryParameter[Option[Double]]) => simpleParam(qp.name, "query", qp.description, false, "number", Some("double"), defaultOf(qp), enumOfOption(qp))

  implicit val booleanReqQueryParamFormat: ParameterJsonFormat[QueryParameter[Boolean]] =
    (qp: QueryParameter[Boolean]) => simpleParam(qp.name, "query", qp.description, true, "boolean", None, None, enumOf(qp))

  implicit val booleanOptQueryParamFormat: ParameterJsonFormat[QueryParameter[Option[Boolean]]] =
    (qp: QueryParameter[Option[Boolean]]) => simpleParam(qp.name, "query", qp.description, false, "boolean", None, defaultOf(qp), enumOfOption(qp))

  implicit val intReqQueryParamFormat: ParameterJsonFormat[QueryParameter[Int]] =
    (qp: QueryParameter[Int]) => simpleParam(qp.name, "query", qp.description, true, "integer", Some("int32"), None, enumOf(qp))

  implicit val intOptQueryParamFormat: ParameterJsonFormat[QueryParameter[Option[Int]]] =
    (qp: QueryParameter[Option[Int]]) => simpleParam(qp.name, "query", qp.description, false, "integer", Some("int32"), defaultOf(qp), enumOfOption(qp))

  implicit val longReqQueryParamFormat: ParameterJsonFormat[QueryParameter[Long]] =
    (qp: QueryParameter[Long]) => simpleParam(qp.name, "query", qp.description, true, "integer", Some("int64"), None, enumOf(qp))

  implicit val longOptQueryParamFormat: ParameterJsonFormat[QueryParameter[Option[Long]]] =
    (qp: QueryParameter[Option[Long]]) => simpleParam(qp.name, "query", qp.description, false, "integer", Some("int64"), defaultOf(qp), enumOfOption(qp))

  implicit val strReqPathParamFormat: ParameterJsonFormat[PathParameter[String]] =
    (pp: PathParameter[String]) => simpleParam(pp.name, "path", pp.description, true, "string", None, None, enumOf(pp))

  implicit val floatReqPathParamFormat: ParameterJsonFormat[PathParameter[Float]] =
    (pp: PathParameter[Float]) => simpleParam(pp.name, "path", pp.description, true, "number", Some("float"), None, enumOf(pp))

  implicit val doubleReqPathParamFormat: ParameterJsonFormat[PathParameter[Double]] =
    (pp: PathParameter[Double]) => simpleParam(pp.name, "path", pp.description, true, "number", Some("double"), None, enumOf(pp))

  implicit val booleanReqPathParamFormat: ParameterJsonFormat[PathParameter[Boolean]] =
    (pp: PathParameter[Boolean]) => simpleParam(pp.name, "path", pp.description, true, "boolean", None, None, enumOf(pp))

  implicit val intReqPathParamFormat: ParameterJsonFormat[PathParameter[Int]] =
    (pp: PathParameter[Int]) => simpleParam(pp.name, "path", pp.description, true, "integer", Some("int32"), None, enumOf(pp))

  implicit val longReqPathParamFormat: ParameterJsonFormat[PathParameter[Long]] =
    (pp: PathParameter[Long]) => simpleParam(pp.name, "path", pp.description, true, "integer", Some("int64"), None, enumOf(pp))

  implicit val strReqHeaderParamFormat: ParameterJsonFormat[HeaderParameter[String]] =
    (hp: HeaderParameter[String]) => simpleParam(hp.name, "header", hp.description, true, "string", None, None, enumOf(hp))

  implicit val strOptHeaderParamFormat: ParameterJsonFormat[HeaderParameter[Option[String]]] =
    (hp: HeaderParameter[Option[String]]) => simpleParam(hp.name, "header", hp.description, false, "string", None, defaultOf(hp), enumOfOption(hp))

  implicit val floatReqHeaderParamFormat: ParameterJsonFormat[HeaderParameter[Float]] =
    (hp: HeaderParameter[Float]) => simpleParam(hp.name, "header", hp.description, true, "number", Some("float"), None, enumOf(hp))

  implicit val floatOptHeaderParamFormat: ParameterJsonFormat[HeaderParameter[Option[Float]]] =
    (hp: HeaderParameter[Option[Float]]) => simpleParam(hp.name, "header", hp.description, false, "number", Some("float"), defaultOf(hp), enumOfOption(hp))

  implicit val doubleReqHeaderParamFormat: ParameterJsonFormat[HeaderParameter[Double]] =
    (hp: HeaderParameter[Double]) => simpleParam(hp.name, "header", hp.description, true, "number", Some("double"), None, enumOf(hp))

  implicit val doubleOptHeaderParamFormat: ParameterJsonFormat[HeaderParameter[Option[Double]]] =
    (hp: HeaderParameter[Option[Double]]) => simpleParam(hp.name, "header", hp.description, false, "number", Some("double"), defaultOf(hp), enumOfOption(hp))

  implicit val booleanReqHeaderParamFormat: ParameterJsonFormat[HeaderParameter[Boolean]] =
    (hp: HeaderParameter[Boolean]) => simpleParam(hp.name, "header", hp.description, true, "boolean", None, None, enumOf(hp))

  implicit val booleanOptHeaderParamFormat: ParameterJsonFormat[HeaderParameter[Option[Boolean]]] =
    (hp: HeaderParameter[Option[Boolean]]) => simpleParam(hp.name, "header", hp.description, false, "boolean", None, defaultOf(hp), enumOfOption(hp))

  implicit val intReqHeaderParamFormat: ParameterJsonFormat[HeaderParameter[Int]] =
    (hp: HeaderParameter[Int]) => simpleParam(hp.name, "header", hp.description, true, "integer", Some("int32"), None, enumOf(hp))

  implicit val intOptHeaderParamFormat: ParameterJsonFormat[HeaderParameter[Option[Int]]] =
    (hp: HeaderParameter[Option[Int]]) => simpleParam(hp.name, "header", hp.description, false, "integer", Some("int32"), defaultOf(hp), enumOfOption(hp))

  implicit val longReqHeaderParamFormat: ParameterJsonFormat[HeaderParameter[Long]] =
    (hp: HeaderParameter[Long]) => simpleParam(hp.name, "header", hp.description, true, "integer", Some("int64"), None, enumOf(hp))

  implicit val longOptHeaderParamFormat: ParameterJsonFormat[HeaderParameter[Option[Long]]] =
    (hp: HeaderParameter[Option[Long]]) => simpleParam(hp.name, "header", hp.description, false, "integer", Some("int64"), defaultOf(hp), enumOfOption(hp))

  implicit def requiredBodyParamFormat[T: TypeTag](implicit ev: SchemaWriter[T]): ParameterJsonFormat[BodyParameter[T]] =
    func2Format((bp: BodyParameter[T]) => bodyParameter(ev, bp.name, bp.description, true, None))

  implicit def optionalBodyParamFormat[T: TypeTag: JsonWriter](implicit ev: SchemaWriter[T]): ParameterJsonFormat[BodyParameter[Option[T]]] =
    func2Format((bp: BodyParameter[Option[T]]) => bodyParameter(ev, bp.name, bp.description, false, defaultOf(bp)))


  private def bodyParameter[T: TypeTag](ev: SchemaWriter[T], name: Symbol,
                                        description: Option[String], required: Boolean,
                                        default: Option[JsValue] = None,
                                        enum: Option[JsValue] = None) = {
    jsObject(
      Some("name" -> JsString(name.name)),
      Some("in" -> JsString("body")),
      description.map("description" -> JsString(_)),
      Some("required" -> JsBoolean(required)),
      default.map("default" -> _),
      Some("schema" -> ev.write(JsonSchema[T]()))
    )
  }

  implicit val hNilParamFormat: ParameterJsonFormat[HNil] =
    _ => JsArray()

  implicit def hConsParamFormat[H, T <: HList](implicit head: ParameterJsonFormat[H], tail: ParameterJsonFormat[T]): ParameterJsonFormat[H :: T] =
    func2Format((l: H :: T) => {
      Flattener.flattenToArray(JsArray(head.write(l.head), tail.write(l.tail)))
    })

  private def simpleParam(name: Symbol, in: String, description: Option[String],
                          required: Boolean, `type`: String, format: Option[String],
                          default: Option[JsValue] = None,
                          enum: Option[JsValue] = None): JsValue =
    jsObject(
      Some("name" -> JsString(name.name)),
      Some("in" -> JsString(in)),
      description.map("description" -> JsString(_)),
      Some("required" -> JsBoolean(required)),
      Some("type" -> JsString(`type`)),
      format.map("format" -> JsString(_)),
      default.map("default" -> _),
      enum.map("enum" -> _)
    )

  private def defaultOf[T: JsonWriter](param: Parameter[Option[T]]): Option[JsValue] =
    param.default.flatten.map(_.toJson)

  private def enumOfOption[T: JsonFormat](param: QueryParameter[Option[T]]): Option[JsValue] =
    param.enum.map(seq => seq.flatten).map(_.toJson)

  private def enumOfOption[T: JsonFormat](param: HeaderParameter[Option[T]]): Option[JsValue] =
    param.enum.map(seq => seq.flatten).map(_.toJson)

  private def enumOf[T: JsonFormat](param: QueryParameter[T]): Option[JsValue] =
    param.enum.map(_.toJson)

  private def enumOf[T: JsonFormat](param: PathParameter[T]): Option[JsValue] =
    param.enum.map(_.toJson)

  private def enumOf[T: JsonFormat](param: HeaderParameter[T]): Option[JsValue] =
    param.enum.map(_.toJson)
}

object ParametersJsonProtocol extends ParametersJsonProtocol