package net.jtownson.swakka.routegen

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import net.jtownson.swakka.OpenApiModel.OpenApi
import spray.json.JsonFormat

object SwaggerRoute {

  def swaggerRoute[Paths](api: OpenApi[Paths])
                             (implicit ev: JsonFormat[OpenApi[Paths]]): Route = {

    get {
      path("swagger.json") {
        complete(ev.write(api).prettyPrint)
      }
    }
  }
}
