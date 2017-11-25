package net.jtownson.swakka

import akka.http.scaladsl.model.ContentTypes.`application/json`
import akka.http.scaladsl.model.{HttpEntity, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import spray.json._

import net.jtownson.swakka.openapimodel._
import net.jtownson.swakka.routegen.PathHandling._


package object routegen extends CorsUseCases {

  /**
    * This is the hook to generate a Route for an OpenApi definition.
    * @param api the swagger model
    * @param swaggerRouteSettings a settings object that dictates whether to include the swagger file itself in the Route
    *                             (along with the URL at which to serve it and which CORS headers to set).
    * @tparam Paths A Paths HList. Please refer to the the project readme or sample code for usage.
    * @tparam SecurityDefinitions A HList of SecurityDefinitions. Again, please refer to the readme or samples.
    * @return An Akka-Http Route that extracts the parameters in the api definition, passing them to the endpoints
    *         defined therein.
    */
  def openApiRoute[Paths, SecurityDefinitions]
  (api: OpenApi[Paths, SecurityDefinitions], swaggerRouteSettings: Option[SwaggerRouteSettings] = None)
  (implicit ev1: RouteGen[Paths], ev2: JsonFormat[OpenApi[Paths, SecurityDefinitions]]): Route =
    hostDirective(api.host) {
      schemesDirective(api.schemes) {
        basePathDirective(api.basePath) {
          swaggerRouteSettings map {
            ev1.toRoute(api.paths) ~ swaggerRoute(api, _)
          } getOrElse {
            ev1.toRoute(api.paths)
          }
        }
      }
    }

  /**
    * swaggerRoute defines a Route that serves the swagger file, generated for an API definition.
    * This endpoint will serve the swagger file without appending any base path defined in the swagger
    * and will not check host or protocol requirements in the swagger.
    * swaggerRoute is called from within openApiRoute to provide an overall Route combining the API
    * definition and the swagger metadata. In that context requests hosts and protcols are checked
    * and the swagger basePath is appended (e.g. if you swagger file defines basePath as /root and
    * swaggerRouteSettings define the swagger path as /swagger.json, swaggerRoute will serve the
    * file from /swagger.json, whereas openApiRoute will serve the file from /root/swagger.json.
    *
    * @param api the API definition
    * @param swaggerRouteSettings CORS and endpoint URL settings for the Route
    * @tparam Paths
    * @tparam SecurityDefinitions
    * @return An Akka-Http Route serving the swagger file.
    */
  def swaggerRoute[Paths, SecurityDefinitions]
  (api: OpenApi[Paths, SecurityDefinitions], swaggerRouteSettings: SwaggerRouteSettings)
  (implicit ev: JsonFormat[OpenApi[Paths, SecurityDefinitions]]): Route = {

    get {
      pathWithSplit(swaggerRouteSettings.endpointPath) {
        complete(HttpResponse(
          status = StatusCodes.OK,
          headers = swaggerRouteSettings.corsUseCase.headers,
          entity = HttpEntity(`application/json`, ev.write(api).prettyPrint))
        )
      }
    }
  }
}
