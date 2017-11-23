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

import net.jtownson.swakka.openapimodel._
import SecurityDefinitionsJsonProtocol._

import org.scalatest.Matchers._
import org.scalatest.FlatSpec

import spray.json._

import shapeless._
import shapeless.syntax.singleton._

class SecurityDefinitionsJsonProtocolSpec extends FlatSpec {

  "JsonProtocol" should "serialize BasicAuthenticationSecurity" in {
    BasicAuthenticationSecurity(Some("description")).toJson shouldBe JsObject(
      "type" -> JsString("basic"),
      "description" -> JsString("description")
    )
  }

  it should "serialize ApiKeyInQuerySecurity" in {
    ApiKeyInQuerySecurity(name = "api_key", Some("description")).toJson shouldBe JsObject(
      "type" -> JsString("apiKey"),
      "name" -> JsString("api_key"),
      "in" -> JsString("query"),
      "description" -> JsString("description")
    )
  }

  it should "serialize ApiKeyInHeaderSecurity" in {
    val securityDefinitions =
      ApiKeyInHeaderSecurity(name = "api_key", Some("description"))

    securityDefinitions.toJson shouldBe JsObject(
      "type" -> JsString("apiKey"),
      "name" -> JsString("api_key"),
      "in" -> JsString("header"),
      "description" -> JsString("description")
    )
  }

  it should "serialize Oauth2ImplicitSecurity" in {
    val securityDefinitions =
      Oauth2ImplicitSecurity(
        authorizationUrl = "authUrl",
        scopes = Some(Map("write:pets" -> "modify pets in your account", "read:pets" -> "read your pets")),
        description = Some("description"))

    securityDefinitions.toJson shouldBe JsObject(
      "type" -> JsString("oauth2"),
      "authorizationUrl" -> JsString("authUrl"),
      "flow" -> JsString("implicit"),
      "scopes" -> JsObject(
        "write:pets" -> JsString("modify pets in your account"),
        "read:pets" -> JsString("read your pets")
      ),
      "description" -> JsString("description")
    )
  }

  it should "serialize Oauth2ApplicationSecurity" in {
    val securityDefinitions =
      Oauth2ApplicationSecurity(
        tokenUrl = "tokenUrl",
        scopes = Some(Map("write:pets" -> "modify pets in your account", "read:pets" -> "read your pets")),
        description = Some("description"))

    securityDefinitions.toJson shouldBe JsObject(
      "type" -> JsString("oauth2"),
      "tokenUrl" -> JsString("tokenUrl"),
      "flow" -> JsString("application"),
      "scopes" -> JsObject(
        "write:pets" -> JsString("modify pets in your account"),
        "read:pets" -> JsString("read your pets")
      ),
      "description" -> JsString("description")
    )
  }

  it should "serialize Oauth2PasswordSecurity" in {
    val securityDefinitions =
      Oauth2PasswordSecurity(
        tokenUrl = "tokenUrl",
        scopes = Some(Map("write:pets" -> "modify pets in your account", "read:pets" -> "read your pets")),
        description = Some("description"))

    securityDefinitions.toJson shouldBe JsObject(
      "type" -> JsString("oauth2"),
      "tokenUrl" -> JsString("tokenUrl"),
      "flow" -> JsString("password"),
      "scopes" -> JsObject(
        "write:pets" -> JsString("modify pets in your account"),
        "read:pets" -> JsString("read your pets")
      ),
      "description" -> JsString("description")
    )
  }

  it should "serialize Oauth2AccessCodeSecurity" in {
    val securityDefinitions =
      Oauth2AccessCodeSecurity(
        tokenUrl = "tokenUrl",
        authorizationUrl = "authUrl",
        scopes = Some(Map("write:pets" -> "modify pets in your account", "read:pets" -> "read your pets")),
        description = Some("description"))

    securityDefinitions.toJson shouldBe JsObject(
      "type" -> JsString("oauth2"),
      "tokenUrl" -> JsString("tokenUrl"),
      "authorizationUrl" -> JsString("authUrl"),
      "flow" -> JsString("accessCode"),
      "scopes" -> JsObject(
        "write:pets" -> JsString("modify pets in your account"),
        "read:pets" -> JsString("read your pets")
      ),
      "description" -> JsString("description")
    )
  }

  it should "serialize a complete security record" in {

    val oauth2ImplicitSecurity = Oauth2ImplicitSecurity(
      authorizationUrl = "authUrl",
      scopes = Some(Map("write:pets" -> "modify pets in your account", "read:pets" -> "read your pets")))

    val apiKeyInHeaderSecurity = ApiKeyInHeaderSecurity(name = "api_key")

    val securityDefinitions =
      ('petstore_auth ->> oauth2ImplicitSecurity) ::
        ('api_key ->> apiKeyInHeaderSecurity) ::
        HNil

    securityDefinitions.toJson shouldBe JsObject(
      "petstore_auth" -> JsObject(
        "type" -> JsString("oauth2"),
        "authorizationUrl" -> JsString("authUrl"),
        "flow" -> JsString("implicit"),
        "scopes" -> JsObject(
          "write:pets" -> JsString("modify pets in your account"),
          "read:pets" -> JsString("read your pets")
        )
      ),
      "api_key" -> JsObject(
        "type" -> JsString("apiKey"),
        "name" -> JsString("api_key"),
        "in" -> JsString("header")
      )
    )
  }

  it should "serialize a security requirement" in {

    val securityRequirement = SecurityRequirement('auth, Seq("grant1", "grant2"))

    val expectedJson = JsObject(
      "auth" -> JsArray(JsString("grant1"), JsString("grant2")
      )
    )

    securityRequirement.toJson shouldBe expectedJson
  }
}
