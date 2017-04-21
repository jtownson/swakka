package net.jtownson.swakka.routegen

import akka.http.scaladsl.server.Directive0
import akka.http.scaladsl.server.Directives._

object schemesDirective {
  def apply(apiSchemes: Option[Seq[String]]): Directive0 = {
    apiSchemes match {
      case Some(schemes) => schemes.foldLeft(reject.toDirective[Unit])(appendScheme)
      case None => pass
    }
  }

  private def appendScheme(d: Directive0, s: String): Directive0 = d | scheme(s)
}