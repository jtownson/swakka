package net.jtownson.swakka

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import net.jtownson.swakka.OpenApiModel.OpenApi
import shapeless.HList
import spray.json.JsonFormat

object SwaggerRoute {

  def swaggerRoute[Endpoints <: HList](api: OpenApi[Endpoints])
                                      (implicit ev: JsonFormat[OpenApi[Endpoints]]): Route = {

    get {
      path("swagger.json") {
        complete(ev.write(api).prettyPrint)
      }
    }
  }
}
