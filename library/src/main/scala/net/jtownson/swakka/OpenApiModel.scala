package net.jtownson.swakka

import akka.http.scaladsl.model.HttpMethod
import akka.http.scaladsl.server.Route
import net.jtownson.swakka.model.{Info, Tag}
import net.jtownson.swakka.model.ModelDefaults._
import net.jtownson.swakka.model.SecurityDefinitions.SecurityRequirement
import net.jtownson.swakka.routegen.ConvertibleToDirective
import shapeless.{HList, HNil}

object OpenApiModel {

  case class Operation[Params <: HList : ConvertibleToDirective, Responses](
    summary: Option[String] = None,
    description: Option[String] = None,
    operationId: Option[String] = None,
    tags: Option[Seq[String]] = None,
    consumes: Option[Seq[String]] = None,
    produces: Option[Seq[String]] = None,
    parameters: Params = HNil,
    responses: Responses = HNil,
    security: Option[Seq[SecurityRequirement]] = None,
    endpointImplementation: Params => Route)

  case class PathItem[Params <: HList : ConvertibleToDirective, Responses](
    path: String,
    method: HttpMethod,
    operation: Operation[Params, Responses])

  case class OpenApi[Paths, +SecurityDefinitions] (
    info: Info = pointlessInfo,
    host: Option[String] = None,
    basePath: Option[String] = None,
    tags: Option[Seq[Tag]] = None,
    schemes: Option[Seq[String]] = None,
    consumes: Option[Seq[String]] = None,
    produces: Option[Seq[String]] = None,
    paths: Paths,
    securityDefinitions: Option[SecurityDefinitions] = None)
}

