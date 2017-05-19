package net.jtownson.swakka.routegen

import java.util.regex.Pattern

import akka.http.scaladsl.model.Uri.Path
import akka.http.scaladsl.server.Directives.{path, _}
import akka.http.scaladsl.server.PathMatcher.Matched
import akka.http.scaladsl.server._
import akka.http.scaladsl.server.util.Tuple

object PathHandling {
  private val notBlank = (s: String) => !s.trim.isEmpty

  private val paramPattern = Pattern.compile("\\{\\s*.+\\s*\\}")

  def splitPath(requestPath: String): List[String] =
    requestPath.split("/").filter(notBlank).toList

  def isAnyParamToken(pathSegment: String): Boolean =
    paramPattern.matcher(pathSegment).matches()

  def isParamToken(pathSegment: String, paramName: String): Boolean =
    pathSegment.matches(s"\\{\\s*$paramName\\s*\\}")

  def containsParamToken(modelPath: String): Boolean =
    splitPath(modelPath).exists(isAnyParamToken)

  def allMatcher[L](l: L)(implicit ev: Tuple[L]): PathMatcher[L] =
    new PathMatcher[L] {
      def apply(v1: Path): PathMatcher.Matching[L] = Matched(v1, l)
    }

  def withParam(pathSegment: String, paramMatchers: Map[String, PathMatcher[Unit]]): PathMatcher[Unit] = {

    if (isAnyParamToken(pathSegment))
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

  def pathWithParamMatcher[T](modelPath: String, paramName: String, pm: PathMatcher1[T]): PathMatcher1[T] = {

    val modelSegments: List[String] = splitPath(modelPath)

    type Pm1Pm0 = Either[PathMatcher1[T], PathMatcher0]

    val segmentMatchers: Seq[Pm1Pm0] =
      modelSegments.map(segment =>
        if (isParamToken(segment, paramName))
          Left(pm)

        else if (isAnyParamToken(segment))
          Right(Segment.tmap(_ => ()))

        else
          Right(PathMatcher(segment)))

    def f(lhs: Pm1Pm0, rhs: Pm1Pm0): Pm1Pm0 = {
      (lhs, rhs) match {
        case (Left(pm1), Right(pm0)) =>
          Left(pm1 / pm0)

        case (Right(pm0_1), Right(pm0_2)) =>
          Right(pm0_1 / pm0_2)

        case (Right(pm0), Left(pm1)) =>
          Left(pm0 / pm1)

        case (Left(_), Left(_)) =>
          throw new IllegalStateException(s"Duplicate parameter name: $paramName.")
      }
    }

    Slash ~ segmentMatchers.reduceLeft(f).left.get
  }


  def akkaPath(modelPath: String, paramMatchers: Map[String, PathMatcher[Unit]]): Directive[Unit] =
    path(splittingPathMatcher(modelPath, paramMatchers))
}