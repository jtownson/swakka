package net.jtownson.swakka.routegen

import akka.http.scaladsl.model.HttpHeader
import akka.http.scaladsl.model.headers.RawHeader
import scala.collection.immutable.Seq

trait CorsUseCase {
  def headers: Seq[HttpHeader]
}

object CorsUseCases {

  val NoCors = new CorsUseCase {
    def headers = Seq()
  }

  val SwaggerUiOnSameHostAsApplication = NoCors

  val CorsHandledByProxyServer = NoCors

  case class CustomCors(accessControlAllowOrigin: String,
                        accessControlAllowMethods: Seq[String],
                        accessControlAllowHeaders: Seq[String]) extends CorsUseCase {


    private def asHeader(header: String)(vals: Seq[String]): RawHeader =
      RawHeader(header, vals.mkString(","))

    override def headers: Seq[HttpHeader] = {
      Seq(
        Some(RawHeader("Access-Control-Allow-Origin", accessControlAllowOrigin)),
        Option(accessControlAllowMethods).filter(_.nonEmpty).map(asHeader("Access-Control-Allow-Methods")),
        Option(accessControlAllowHeaders).filter(_.nonEmpty).map(asHeader("Access-Control-Allow-Headers")))
        .flatten
    }


  }
}
