package net.jtownson.openapi

object OpenApiModel {

  sealed trait SwaggerVersion

  case object `2.0` extends SwaggerVersion

  case class Contact(name: Option[String],
                     url: Option[String],
                     email: Option[String])

  case class Licence(name: String,
                     url: Option[String] = None)

  case class Info(
                   title: String,
                   version: String,
                   description: Option[String] = None,
                   termsOfService: Option[String] = None,
                   contact: Option[Contact] = None,
                   licence: Option[Licence] = None
                 )

  sealed trait HttpMethod

  case object Get extends HttpMethod

  case object Put extends HttpMethod

  case object Post extends HttpMethod

  case object Delete extends HttpMethod

  case object Options extends HttpMethod

  case object Head extends HttpMethod

  case object Patch extends HttpMethod

  case object Parameters extends HttpMethod

  type MediaTypeList = Seq[String]

  sealed trait Parameter


  case class NonBodyParameter(
                               name: String,
                               `type`: String
                             )

  case class BodyParameter(
                            name: String,
                            required: Boolean
                          )

  case class JsonReference(ref: String) extends Parameter

  type ParametersList = Seq[String]

  case class ExternalDocs(description: String,
                          url: String)

  sealed trait ResponseCode
  case object Default extends ResponseCode
  case class HttpResponseCode(httpStatus: Int) extends ResponseCode
  implicit def int2ResponseCode(httpStatus: Int): HttpResponseCode = HttpResponseCode(httpStatus)

  case class ResponseValue[T](responseCode: ResponseCode, description: String, responseType: T)

  case class Operation(
                        tags: Seq[String] = Nil,
                        summary: Option[String] = None,
                        description: Option[String] = None,
                        externalDocs: Option[ExternalDocs] = None,
                        operationId: Option[String] = None,
                        produces: MediaTypeList = Nil,
                        consumes: MediaTypeList = Nil,
                        parameters: ParametersList = Nil,
                        responses: Seq[ResponseValue[_]],
                        schemes: Seq[String] = Nil,
                        deprecated: Boolean = false
                        /*security: TODO */
                      )

  case class PathItem(value: (HttpMethod, Operation))

  case class OpenApi(
                      swagger: String,
                      info: Info,
                      host: Option[String] = None,
                      basePath: Option[String] = None,
                      schemes: Seq[String] = Nil,
                      consumes: MediaTypeList = Nil,
                      produces: MediaTypeList = Nil,
                      paths: Map[String, PathItem]
                    )

}