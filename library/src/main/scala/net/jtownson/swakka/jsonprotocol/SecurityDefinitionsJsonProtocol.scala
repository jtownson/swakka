package net.jtownson.swakka.jsonprotocol

import net.jtownson.swakka.misc.jsObject
import net.jtownson.swakka.model.SecurityDefinitions._
import shapeless.labelled.FieldType
import shapeless.{::, HList, HNil, Lazy, Witness}
import spray.json.{DefaultJsonProtocol, JsObject, JsString, JsonWriter}
import spray.json._

trait SecurityDefinitionsJsonProtocol extends DefaultJsonProtocol {

  implicit val basicAuthenticationSecurityJsonFormat: JsonWriter[BasicAuthenticationSecurity] =
    security => jsObject(
      Some("type" -> JsString("basic")),
      security.description.map("description" -> JsString(_))
    )

  implicit val apiKeyInQuerySecurityJsonFormat: JsonWriter[ApiKeyInQuerySecurity] =
    security => jsObject(
      Some("type" -> JsString("apiKey")),
      Some("name" -> JsString(security.name)),
      Some("in" -> JsString("query")),
      security.description.map("description" -> JsString(_))
    )

  implicit val apiKeyInHeaderSecurityJsonFormat: JsonWriter[ApiKeyInHeaderSecurity] =
    security => jsObject(
      Some("type" -> JsString("apiKey")),
      Some("name" -> JsString(security.name)),
      Some("in" -> JsString("header")),
      security.description.map("description" -> JsString(_))
    )

  implicit val oauth2ApplicationSecurityJsonFormat: JsonWriter[Oauth2ApplicationSecurity] =
    security => jsObject(
      Some("type" -> JsString("oauth2")),
      Some("flow" -> JsString("application")),
      Some("tokenUrl" -> JsString(security.tokenUrl)),
      security.scopes.map("scopes" -> _.toJson),
      security.description.map("description" -> JsString(_))
    )

  implicit val oauth2ImplicitSecurityJsonFormat: JsonWriter[Oauth2ImplicitSecurity] =
    security => jsObject(
      Some("type" -> JsString("oauth2")),
      Some("flow" -> JsString("implicit")),
      Some("authorizationUrl" -> JsString(security.authorizationUrl)),
      security.scopes.map("scopes" -> _.toJson),
      security.description.map("description" -> JsString(_))
    )

  implicit val oauth2PasswordSecurityJsonFormat: JsonWriter[Oauth2PasswordSecurity] =
    security => jsObject(
      Some("type" -> JsString("oauth2")),
      Some("flow" -> JsString("password")),
      Some("tokenUrl" -> JsString(security.tokenUrl)),
      security.scopes.map("scopes" -> _.toJson),
      security.description.map("description" -> JsString(_))
    )

  implicit val oauth2AccessCodeSecurityJsonFormat: JsonWriter[Oauth2AccessCodeSecurity] =
    security => jsObject(
      Some("type" -> JsString("oauth2")),
      Some("flow" -> JsString("accessCode")),
      Some("tokenUrl" -> JsString(security.tokenUrl)),
      Some("authorizationUrl" -> JsString(security.authorizationUrl)),
      security.scopes.map("scopes" -> _.toJson),
      security.description.map("description" -> JsString(_))
    )

  implicit val hnilWriter: JsonWriter[HNil] =
    _ => JsObject()

  implicit def recordWriter[K <: Symbol, H, T <: HList](implicit
                                                        witness: Witness.Aux[K],
                                                        hWriter: Lazy[JsonWriter[H]],
                                                        tWriter: JsonWriter[T]): JsonWriter[FieldType[K, H] :: T] =
    (hl: FieldType[K, H] :: T) => {
      JsObject(
        JsObject(witness.value.name -> hWriter.value.write(hl.head)).fields ++
          tWriter.write(hl.tail).asInstanceOf[JsObject].fields
      )
    }
}

object SecurityDefinitionsJsonProtocol extends SecurityDefinitionsJsonProtocol