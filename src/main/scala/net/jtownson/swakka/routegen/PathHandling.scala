package net.jtownson.swakka.routegen

import akka.http.scaladsl.server.Directives.{path, _}
import akka.http.scaladsl.server.{Directive, PathMatcher, PathMatchers}

object PathHandling {
  private val notBlank = (s: String) => !s.trim.isEmpty

  private def splitPath(requestPath: String): List[String] =
    requestPath.split("/").filter(notBlank).toList

  def splittingPathMatcher(modelPath: String): PathMatcher[Unit] = {
    def loop(paths: List[String]): PathMatcher[Unit] = paths match {
      case Nil => PathMatchers.Neutral
      case pathSegment :: Nil => PathMatcher(pathSegment)
      case pathSegment :: tail => pathSegment / loop(tail)
    }
    loop(splitPath(modelPath))
  }

  def akkaPath(modelPath: String): Directive[Unit] =
    path(splittingPathMatcher(modelPath))
}