package net.jtownson.swakka.routegen

import akka.http.scaladsl.model.ContentTypes.`application/json`
import akka.http.scaladsl.model.{HttpEntity, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives.{complete, _}
import akka.http.scaladsl.server.Route
import net.jtownson.swakka.OpenApiModel.OpenApi
import net.jtownson.swakka.routegen.PathHandling.pathWithSplit
import spray.json.JsonFormat

object SwaggerRoute {

  def swaggerRoute[Paths](api: OpenApi[Paths], swaggerRouteSettings: SwaggerRouteSettings)
                         (implicit ev: JsonFormat[OpenApi[Paths]]): Route = {

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
