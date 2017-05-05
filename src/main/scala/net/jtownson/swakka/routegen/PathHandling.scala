package net.jtownson.swakka.routegen

import java.util.regex.Pattern

import akka.http.scaladsl.server.Directives.{path, _}
import akka.http.scaladsl.server.{Directive, PathMatcher, PathMatchers}

object PathHandling {
  private val notBlank = (s: String) => !s.trim.isEmpty

  private val paramPattern = Pattern.compile("\\{\\s*.+\\s*\\}")

  private def splitPath(requestPath: String): List[String] =
    requestPath.split("/").filter(notBlank).toList

  def isParamToken(pathSegment: String): Boolean =
    paramPattern.matcher(pathSegment).matches()

  def withParam(pathSegment: String, paramMatchers: Map[String, PathMatcher[Unit]]): PathMatcher[Unit] = {

    if (isParamToken(pathSegment))
      paramMatchers.getOrElse(pathSegment, PathMatcher(pathSegment))
    else
      PathMatcher(pathSegment)
  }

  def splittingPathMatcher(modelPath: String,
                           paramMatchers: Map[String, PathMatcher[Unit]] = Map()): PathMatcher[Unit] = {
    def loop(paths: List[String]): PathMatcher[Unit] = paths match {
      case Nil => PathMatchers.Neutral
      case pathSegment :: Nil => withParam(pathSegment, paramMatchers)
      case pathSegment :: tail => withParam(pathSegment, paramMatchers) / loop(tail)
    }
    loop(splitPath(modelPath))
  }

  def akkaPath(modelPath: String, paramMatchers: Map[String, PathMatcher[Unit]]): Directive[Unit] =
    path(splittingPathMatcher(modelPath, paramMatchers))
}