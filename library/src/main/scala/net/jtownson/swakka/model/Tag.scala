package net.jtownson.swakka.model

case class Tag(name: String, description: Option[String] = None, externalDocs: Option[ExternalDocs] = None)
