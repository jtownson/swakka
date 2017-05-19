package net.jtownson.swakka.routegen

import akka.http.scaladsl.server.Directive0
import akka.http.scaladsl.server.Directives._

object basePathDirective {
  def apply(apiBasePath: Option[String]): Directive0 =
    apiBasePath match {
      case Some(basePath) => pathPrefix(PathHandling.splittingPathMatcher(basePath))
      case None => pass
    }
}
