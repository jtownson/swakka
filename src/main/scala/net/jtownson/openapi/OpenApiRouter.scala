package net.jtownson.openapi

import akka.http.scaladsl.server.Route
import net.jtownson.openapi.OpenApiModel.OpenApi
import spray.json._

import scala.io.Source

object OpenApiRouter {


  def parseOpenApiSource(openApiSource: Source): OpenApi = ??? // JsonParser(openApiSource).convertTo[OpenApi]
  def akkaRoute(openApiDefinition: OpenApi): Route = ???
}
