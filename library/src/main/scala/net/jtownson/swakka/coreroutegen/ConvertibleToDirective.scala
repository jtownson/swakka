package net.jtownson.swakka.coreroutegen

import akka.http.scaladsl.server.Directive1

/**
  * ConvertibleToDirective is a type class supporting the conversion
  * of Api entities (query params, path params, etc) into Akka-Http directives
  * that extract the values of those entities.
  * These Directives are composed by RouteGen into a single Route.
  */
trait ConvertibleToDirective[T] {
  def convertToDirective(modelPath: String, t: T): Directive1[T]
}
