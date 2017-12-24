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
package net.jtownson.swakka.openapimodel

trait Security {
  def key: String
}

case class BasicAuthenticationSecurity(key: String, description: Option[String] = None) extends Security

case class ApiKeyInQuerySecurity(key: String, description: Option[String] = None) extends Security

case class ApiKeyInHeaderSecurity(key: String, description: Option[String] = None) extends Security

case class Oauth2ImplicitSecurity(key: String, authorizationUrl: String, scopes: Option[Map[String, String]] = None, description: Option[String] = None) extends Security

case class Oauth2ApplicationSecurity(key: String, tokenUrl: String, scopes: Option[Map[String, String]] = None, description: Option[String] = None) extends Security

case class Oauth2PasswordSecurity(key: String, tokenUrl: String, scopes: Option[Map[String, String]] = None, description: Option[String] = None) extends Security

case class Oauth2AccessCodeSecurity(key: String, authorizationUrl: String, tokenUrl: String, scopes: Option[Map[String, String]] = None, description: Option[String] = None) extends Security

case class SecurityRequirement(name: Symbol, refs: Seq[String] = Seq())
