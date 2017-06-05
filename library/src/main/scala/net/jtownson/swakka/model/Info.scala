package net.jtownson.swakka.model

case class Info(version: String, title: String,
                description: Option[String] = None, termsOfService: Option[String] = None,
                contact: Option[Contact] = None, licence: Option[License] = None)
