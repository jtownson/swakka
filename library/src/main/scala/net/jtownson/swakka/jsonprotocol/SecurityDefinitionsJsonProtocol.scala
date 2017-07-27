package net.jtownson.swakka.jsonprotocol

import net.jtownson.swakka.misc.jsObject
import net.jtownson.swakka.model.SecurityDefinitions._
import net.jtownson.swakka.jsonprotocol.SecurityDefinitionsJsonFormat._

import shapeless.labelled.FieldType
import shapeless.{::, HList, HNil, Lazy, Witness}
import spray.json.{JsObject, JsString}
import spray.json._
import spray.json.DefaultJsonProtocol._

trait SecurityDefinitionsJsonProtocol {

  implicit val basicAuthenticationSecurityJsonFormat: SecurityDefinitionsJsonFormat[BasicAuthenticationSecurity] =
    func2Format(security => jsObject(
      Some("type" -> JsString("basic")),
      security.description.map("description" -> JsString(_))
    ))

  implicit val apiKeyInQuerySecurityJsonFormat: SecurityDefinitionsJsonFormat[ApiKeyInQuerySecurity] =
    func2Format(security => jsObject(
      Some("type" -> JsString("apiKey")),
      Some("name" -> JsString(security.name)),
      Some("in" -> JsString("query")),
      security.description.map("description" -> JsString(_))
    ))

  implicit val apiKeyInHeaderSecurityJsonFormat: SecurityDefinitionsJsonFormat[ApiKeyInHeaderSecurity] =
    func2Format(security => jsObject(
      Some("type" -> JsString("apiKey")),
      Some("name" -> JsString(security.name)),
      Some("in" -> JsString("header")),
      security.description.map("description" -> JsString(_))
    ))

  implicit val oauth2ApplicationSecurityJsonFormat: SecurityDefinitionsJsonFormat[Oauth2ApplicationSecurity] =
    func2Format(security => jsObject(
      Some("type" -> JsString("oauth2")),
      Some("flow" -> JsString("application")),
      Some("tokenUrl" -> JsString(security.tokenUrl)),
      security.scopes.map("scopes" -> _.toJson),
      security.description.map("description" -> JsString(_))
    ))

  implicit val oauth2ImplicitSecurityJsonFormat: SecurityDefinitionsJsonFormat[Oauth2ImplicitSecurity] =
    func2Format(security => jsObject(
      Some("type" -> JsString("oauth2")),
      Some("flow" -> JsString("implicit")),
      Some("authorizationUrl" -> JsString(security.authorizationUrl)),
      security.scopes.map("scopes" -> _.toJson),
      security.description.map("description" -> JsString(_))
    ))

  implicit val oauth2PasswordSecurityJsonFormat: SecurityDefinitionsJsonFormat[Oauth2PasswordSecurity] =
    func2Format(security => jsObject(
      Some("type" -> JsString("oauth2")),
      Some("flow" -> JsString("password")),
      Some("tokenUrl" -> JsString(security.tokenUrl)),
      security.scopes.map("scopes" -> _.toJson),
      security.description.map("description" -> JsString(_))
    ))

  implicit val oauth2AccessCodeSecurityJsonFormat: SecurityDefinitionsJsonFormat[Oauth2AccessCodeSecurity] =
    func2Format(security => jsObject(
      Some("type" -> JsString("oauth2")),
      Some("flow" -> JsString("accessCode")),
      Some("tokenUrl" -> JsString(security.tokenUrl)),
      Some("authorizationUrl" -> JsString(security.authorizationUrl)),
      security.scopes.map("scopes" -> _.toJson),
      security.description.map("description" -> JsString(_))
    ))

  implicit val hnilWriterRecord: SecurityDefinitionsJsonFormat[HNil] =
    func2Format(_ => JsObject())

  implicit def recordWriter[K <: Symbol, H, T <: HList](implicit
                                                        witness: Witness.Aux[K],
                                                        hWriter: Lazy[SecurityDefinitionsJsonFormat[H]],
                                                        tWriter: SecurityDefinitionsJsonFormat[T]): SecurityDefinitionsJsonFormat[FieldType[K, H] :: T] =
    func2Format((hl: FieldType[K, H] :: T) => {
      JsObject(
        JsObject(witness.value.name -> hWriter.value.write(hl.head)).fields ++
          tWriter.write(hl.tail).asInstanceOf[JsObject].fields
        )
      }
    )
}

object SecurityDefinitionsJsonProtocol extends SecurityDefinitionsJsonProtocol