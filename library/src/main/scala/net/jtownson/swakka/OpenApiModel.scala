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

package net.jtownson.swakka

import akka.http.scaladsl.model.HttpMethod
import shapeless.HNil

trait OpenApiModel extends SecurityDefinitions with Parameters with Responses {

  /**
    * Maps to an OpenAPI operation.
    * @param parameters parameters must be an HList of parameter types found in net.jtownson.swakka.OpenApiModel
    * @param responses responses must a HList of or bare net.jtownson.swakka.OpenApiModel
    * @param security
    * @param endpointImplementation a function of type EndpointImplementation (see below)
    * @tparam Params this is a HList of the parameters collected by your api operation.
    *                e.g. QueryParameter[String] :: HeaderParameter[Long] :: HNil.
    *                These Params affect both the generated swagger json and generated akka-http
    *                Routes which extracts parameters from a user request.
    * @tparam EndpointFunction this is a FunctionN, whose input is dependent on the Params type and returning an akka-http Route.
    *           Given the example params, above, F would be (String, Long) => Route
    * @tparam Responses this is a HList or bare type that defines the responses swagger json.
    */
  case class Operation[Params, EndpointFunction, Responses](
    summary: Option[String] = None,
    description: Option[String] = None,
    operationId: Option[String] = None,
    tags: Option[Seq[String]] = None,
    consumes: Option[Seq[String]] = None,
    produces: Option[Seq[String]] = None,
    parameters: Params = HNil: HNil,
    responses: Responses = HNil: HNil,
    security: Option[Seq[SecurityRequirement]] = None,
    endpointImplementation: EndpointFunction)

  case class PathItem[Params, EndpointFunction, Responses](
    path: String,
    method: HttpMethod,
    operation: Operation[Params, EndpointFunction, Responses])

  case class OpenApi[Paths, +SecurityDefinitions] (
    info: Info = Info(version = "", title = ""),
    host: Option[String] = None,
    basePath: Option[String] = None,
    tags: Option[Seq[Tag]] = None,
    schemes: Option[Seq[String]] = None,
    consumes: Option[Seq[String]] = None,
    produces: Option[Seq[String]] = None,
    paths: Paths,
    securityDefinitions: Option[SecurityDefinitions] = None)

  case class Contact(name: Option[String] = None, url: Option[String] = None, email: Option[String] = None)

  case class ExternalDocs(url: String, description: Option[String] = None)

  case class Info(version: String, title: String,
                  description: Option[String] = None, termsOfService: Option[String] = None,
                  contact: Option[Contact] = None, licence: Option[License] = None)

  case class License(name: String, url: Option[String] = None)

  case class Tag(name: String, description: Option[String] = None, externalDocs: Option[ExternalDocs] = None)

}

object OpenApiModel extends OpenApiModel


trait Parameters {

  sealed trait Parameter[T] {
    def description: Option[String]

    def value: T

    def default: Option[T]

    def enum: Option[Seq[T]]

    def name: Symbol
  }

  sealed trait ClosedParameter[T, U] extends Parameter[T]

  sealed trait OpenParameter[T, U] extends Parameter[T] {
    def value: T = throw new IllegalStateException(
      "The parameter is currently in the state of a template " +
        "without an associated value. Parameters with values " +
        "can only be created by passing http requests through matching route structures. " +
        "(i.e. create an API definition, get its Route then make a request).")

    def closeWith(t: T): U
  }


  sealed trait MultiValued[T, U <: Parameter[T]] extends Parameter[Seq[T]] {
    def singleParam: U
    def enum: Option[Seq[Seq[T]]] = None
  }

  object MultiValued {
    def apply[T, U <: Parameter[T]](singleParam: U, default: Option[Seq[T]] = None): MultiValued[T, U] =
      OpenMultiValued(singleParam, singleParam.name, singleParam.description, default)

    case class OpenMultiValued[T, U <: Parameter[T]](
                                                      singleParam: U,
                                                      name: Symbol,
                                                      description: Option[String],
                                                      default: Option[Seq[T]])
      extends MultiValued[T, U] with OpenParameter[Seq[T], ClosedMultiValued[T, U]] {

      override def closeWith(t: Seq[T]): ClosedMultiValued[T, U] =
        ClosedMultiValued(singleParam, name, description, default, t)
    }

    case class ClosedMultiValued[T, U <: Parameter[T]](singleParam: U,
                                                       name: Symbol,
                                                       description: Option[String],
                                                       default: Option[Seq[T]],
                                                       value: Seq[T])
      extends MultiValued[T, U] with ClosedParameter[Seq[T], ClosedMultiValued[T, U]]
  }

  sealed trait FormFieldParameter[T] extends Parameter[T]

  object FormFieldParameter {

    def apply[T](name: Symbol, description: Option[String] = None, default: Option[T] = None,
                 enum: Option[Seq[T]] = None): FormFieldParameter[T] =
      OpenFormFieldParameter(name, description, default, enum)

    def unapply[T](fp: FormFieldParameter[T]): Option[T] = fp match {
      case OpenFormFieldParameter(_, _, default, _) => default
      case ClosedFormFieldParameter(_, _, _, _, value) => Some(value)
      case _ => None
    }

    case class OpenFormFieldParameter[T](name: Symbol,
                                         description: Option[String],
                                         default: Option[T],
                                         enum: Option[Seq[T]])
      extends FormFieldParameter[T] with OpenParameter[T, ClosedFormFieldParameter[T]] {

      override def closeWith(t: T): ClosedFormFieldParameter[T] =
        ClosedFormFieldParameter(name, description, default, enum, t)
    }

    case class ClosedFormFieldParameter[T](name: Symbol,
                                           description: Option[String],
                                           default: Option[T],
                                           enum: Option[Seq[T]],
                                           value: T)
      extends FormFieldParameter[T] with ClosedParameter[T, ClosedFormFieldParameter[T]]
  }


  sealed trait QueryParameter[T] extends Parameter[T]

  object QueryParameter {

    def apply[T](name: Symbol, description: Option[String] = None,
                 default: Option[T] = None, enum: Option[Seq[T]] = None): QueryParameter[T] =
      OpenQueryParameter(name, description, default, enum)

    def unapply[T](qp: QueryParameter[T]): Option[T] = qp match {
      case OpenQueryParameter(_, _, default, _) => default
      case ClosedQueryParameter(_, _, _, _, value) => Some(value)
    }

    case class OpenQueryParameter[T](name: Symbol, description: Option[String],
                                     default: Option[T], enum: Option[Seq[T]])
      extends QueryParameter[T] with OpenParameter[T, ClosedQueryParameter[T]] {
      override def closeWith(t: T): ClosedQueryParameter[T] =
        ClosedQueryParameter(name, description, default, enum, t)
    }

    case class ClosedQueryParameter[T](name: Symbol, description: Option[String],
                                       default: Option[T], enum: Option[Seq[T]], value: T)
      extends QueryParameter[T] with ClosedParameter[T, ClosedQueryParameter[T]]

  }

  sealed trait PathParameter[T] extends Parameter[T]

  object PathParameter {

    def apply[T](name: Symbol, description: Option[String] = None,
                 default: Option[T] = None, enum: Option[Seq[T]] = None): PathParameter[T] =
      OpenPathParameter(name, description, default, enum)

    def unapply[T](pp: PathParameter[T]): Option[T] = pp match {
      case OpenPathParameter(_, _, default, _) => default
      case ClosedPathParameter(_, _, _, _, value) => Some(value)
    }

    case class OpenPathParameter[T](name: Symbol, description: Option[String],
                                    default: Option[T], enum: Option[Seq[T]])
      extends PathParameter[T] with OpenParameter[T, ClosedPathParameter[T]] {

      override def closeWith(t: T): ClosedPathParameter[T] =
        ClosedPathParameter(name, description, default, enum, t)
    }

    case class ClosedPathParameter[T](name: Symbol, description: Option[String],
                                      default: Option[T], enum: Option[Seq[T]], value: T)
      extends PathParameter[T] with ClosedParameter[T, ClosedPathParameter[T]]

  }

  sealed trait BodyParameter[T] extends Parameter[T]

  object BodyParameter {

    def apply[T](name: Symbol, description: Option[String] = None,
                 default: Option[T] = None, enum: Option[Seq[T]] = None): BodyParameter[T] =
      OpenBodyParameter(name, description, default, enum)

    def unapply[T](bp: BodyParameter[T]): Option[T] = bp match {
      case OpenBodyParameter(_, _, default, _) => default
      case ClosedBodyParameter(_, _, _, _, value) => Some(value)
    }

    case class OpenBodyParameter[T](name: Symbol, description: Option[String],
                                    default: Option[T], enum: Option[Seq[T]])
      extends BodyParameter[T] with OpenParameter[T, ClosedBodyParameter[T]] {

      override def closeWith(t: T): ClosedBodyParameter[T] =
        ClosedBodyParameter(name, description, default, enum, t)
    }

    case class ClosedBodyParameter[T](name: Symbol, description: Option[String],
                                      default: Option[T], enum: Option[Seq[T]], value: T)
      extends BodyParameter[T] with ClosedParameter[T, ClosedBodyParameter[T]]

  }

  sealed trait HeaderParameter[T] extends Parameter[T]

  object HeaderParameter {

    def apply[T](name: Symbol, description: Option[String] = None,
                 default: Option[T] = None, enum: Option[Seq[T]] = None):
    HeaderParameter[T] = OpenHeaderParameter(name, description, default, enum)

    def unapply[T](hp: HeaderParameter[T]): Option[T] = hp match {
      case OpenHeaderParameter(_, _, default, _) => default
      case ClosedHeaderParameter(_, _, _, _, value) => Some(value)
    }

    case class OpenHeaderParameter[T](name: Symbol, description: Option[String],
                                      default: Option[T], enum: Option[Seq[T]])
      extends HeaderParameter[T] with OpenParameter[T, ClosedHeaderParameter[T]] {

      override def closeWith(t: T): ClosedHeaderParameter[T] =
        ClosedHeaderParameter(name, description, default, enum, t)
    }

    case class ClosedHeaderParameter[T](name: Symbol, description: Option[String],
                                        default: Option[T], enum: Option[Seq[T]], value: T)
      extends HeaderParameter[T] with ClosedParameter[T, ClosedHeaderParameter[T]]
  }
}

trait Responses {

  case class Header[T](name: Symbol, description: Option[String] = None)

  case class ResponseValue[T, Headers](responseCode: String, description: String, headers: Headers = HNil)
}

trait SecurityDefinitions {

  case class BasicAuthenticationSecurity(description: Option[String] = None)

  case class ApiKeyInQuerySecurity(name: String, description: Option[String] = None)

  case class ApiKeyInHeaderSecurity(name: String, description: Option[String] = None)

  case class Oauth2ImplicitSecurity(authorizationUrl: String, scopes: Option[Map[String, String]] = None, description: Option[String] = None)

  case class Oauth2ApplicationSecurity(tokenUrl: String, scopes: Option[Map[String, String]] = None, description: Option[String] = None)

  case class Oauth2PasswordSecurity(tokenUrl: String, scopes: Option[Map[String, String]] = None, description: Option[String] = None)

  case class Oauth2AccessCodeSecurity(authorizationUrl: String, tokenUrl: String, scopes: Option[Map[String, String]] = None, description: Option[String] = None)

  case class SecurityRequirement(name: Symbol, refs: Seq[String] = Seq())
}
