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

import net.jtownson.swakka.misc.jsObject
import net.jtownson.swakka.openapimodel._
import net.jtownson.swakka.openapijson.SecurityDefinitionsJsonFormat._
import shapeless.labelled.FieldType
import shapeless.{::, HList, HNil, LabelledGeneric, Lazy, Witness, |¬|}
import spray.json.{JsObject, JsString}
import spray.json._
import spray.json.DefaultJsonProtocol._

trait SecurityDefinitionsJsonProtocol {

  implicit val basicAuthenticationSecurityJsonFormat: SecurityDefinitionsJsonFormat[BasicAuthenticationSecurity] =
    instance(security => jsObject(
      Some("type" -> JsString("basic")),
      security.description.map("description" -> JsString(_))
    ))

  implicit val apiKeyInQuerySecurityJsonFormat: SecurityDefinitionsJsonFormat[ApiKeyInQuerySecurity] =
    instance(security => jsObject(
      Some("type" -> JsString("apiKey")),
      Some("name" -> JsString(security.name)),
      Some("in" -> JsString("query")),
      security.description.map("description" -> JsString(_))
    ))

  implicit val apiKeyInHeaderSecurityJsonFormat: SecurityDefinitionsJsonFormat[ApiKeyInHeaderSecurity] =
    instance(security => jsObject(
      Some("type" -> JsString("apiKey")),
      Some("name" -> JsString(security.name)),
      Some("in" -> JsString("header")),
      security.description.map("description" -> JsString(_))
    ))

  implicit val oauth2ApplicationSecurityJsonFormat: SecurityDefinitionsJsonFormat[Oauth2ApplicationSecurity] =
    instance(security => jsObject(
      Some("type" -> JsString("oauth2")),
      Some("flow" -> JsString("application")),
      Some("tokenUrl" -> JsString(security.tokenUrl)),
      security.scopes.map("scopes" -> _.toJson),
      security.description.map("description" -> JsString(_))
    ))

  implicit val oauth2ImplicitSecurityJsonFormat: SecurityDefinitionsJsonFormat[Oauth2ImplicitSecurity] =
    instance(security => jsObject(
      Some("type" -> JsString("oauth2")),
      Some("flow" -> JsString("implicit")),
      Some("authorizationUrl" -> JsString(security.authorizationUrl)),
      security.scopes.map("scopes" -> _.toJson),
      security.description.map("description" -> JsString(_))
    ))

  implicit val oauth2PasswordSecurityJsonFormat: SecurityDefinitionsJsonFormat[Oauth2PasswordSecurity] =
    instance(security => jsObject(
      Some("type" -> JsString("oauth2")),
      Some("flow" -> JsString("password")),
      Some("tokenUrl" -> JsString(security.tokenUrl)),
      security.scopes.map("scopes" -> _.toJson),
      security.description.map("description" -> JsString(_))
    ))

  implicit val oauth2AccessCodeSecurityJsonFormat: SecurityDefinitionsJsonFormat[Oauth2AccessCodeSecurity] =
    instance(security => jsObject(
      Some("type" -> JsString("oauth2")),
      Some("flow" -> JsString("accessCode")),
      Some("tokenUrl" -> JsString(security.tokenUrl)),
      Some("authorizationUrl" -> JsString(security.authorizationUrl)),
      security.scopes.map("scopes" -> _.toJson),
      security.description.map("description" -> JsString(_))
    ))

  implicit val securityRequirementJsonFormat: SecurityDefinitionsJsonFormat[SecurityRequirement] =
    (securityRequirement: SecurityRequirement) => {
      JsObject(securityRequirement.name.name -> JsArray(securityRequirement.refs.map(JsString(_)).toList: _*))
    }

  implicit val hnilWriterRecord: SecurityDefinitionsJsonFormat[HNil] =
    instance(_ => JsObject())

  implicit def recordWriter[K <: Symbol, H, T <: HList](implicit
                                                        witness: Witness.Aux[K],
                                                        hWriter: Lazy[SecurityDefinitionsJsonFormat[H]],
                                                        tWriter: SecurityDefinitionsJsonFormat[T])
  : SecurityDefinitionsJsonFormat[FieldType[K, H] :: T] =
    instance((hl: FieldType[K, H] :: T) => {
      JsObject(
        JsObject(witness.value.name -> hWriter.value.write(hl.head)).fields ++
          tWriter.write(hl.tail).asInstanceOf[JsObject].fields
      )
    }
    )

  implicit def genericSecurityFormat[Defs: |¬|[Security]#λ, DefsRecord]
  (implicit gen: LabelledGeneric.Aux[Defs, DefsRecord],
   ev: Lazy[SecurityDefinitionsJsonFormat[DefsRecord]])
  : SecurityDefinitionsJsonFormat[Defs] = instance(defs => ev.value.write(gen.to(defs)))

}

object SecurityDefinitionsJsonProtocol extends SecurityDefinitionsJsonProtocol