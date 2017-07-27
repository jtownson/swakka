package net.jtownson.swakka.model

object SecurityDefinitions {

  case class BasicAuthenticationSecurity(description: Option[String] = None)

  case class ApiKeyInQuerySecurity(name: String, description: Option[String] = None)

  case class ApiKeyInHeaderSecurity(name: String, description: Option[String] = None)

  case class Oauth2ImplicitSecurity(authorizationUrl: String, scopes: Option[Map[String, String]] = None, description: Option[String] = None)

  case class Oauth2ApplicationSecurity(tokenUrl: String, scopes: Option[Map[String, String]] = None, description: Option[String] = None)

  case class Oauth2PasswordSecurity(tokenUrl: String, scopes: Option[Map[String, String]] = None, description: Option[String] = None)

  case class Oauth2AccessCodeSecurity(authorizationUrl: String, tokenUrl: String, scopes: Option[Map[String, String]] = None, description: Option[String] = None)

  case class SecurityRequirement(name: Symbol, refs: Seq[String] = Seq())
}
