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

  def isParamToken(pathSegment: String): Boolean =
    paramPattern.matcher(pathSegment).matches()


  def allMatcher[L](l: L)(implicit ev: Tuple[L]): PathMatcher[L] =
    new PathMatcher[L] {
      def apply(v1: Path): PathMatcher.Matching[L] = Matched(v1, l)
    }

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

  def combine3(): PathMatcher1[Int] = {

// in hard coded terms, we want this:
//    val path: String = "foo/123/bar"
//
//    val matcher: PathMatcher[Tuple1[Int]] = Slash ~ PathMatcher("foo") / IntNumber / PathMatcher("bar")

    val modelSegments: List[String] = List("foo", "{param}", "bar")

    type Pm1Pm0 = Either[PathMatcher1[Int], PathMatcher0]

    val segmentMatchers: Seq[Pm1Pm0] =
      modelSegments.map(segment => if (isParamToken(segment)) Left(IntNumber) else Right(PathMatcher(segment)))

    def f(lhs: Pm1Pm0, rhs: Pm1Pm0): Pm1Pm0 = {
      (lhs, rhs) match {
        case (Left(pm1), Right(pm0)) => Left(pm1 / pm0)
        case (Right(pm0_1), Right(pm0_2)) => Right(pm0_1 / pm0_2)
        case (Right(pm0), Left(pm1)) => Left(pm0 / pm1)
        case (Left(pm1_1), Left(pm1_2)) => ???
      }
    }

    Slash ~ segmentMatchers.reduceLeft(f).left.get

  }


  def akkaPath(modelPath: String, paramMatchers: Map[String, PathMatcher[Unit]]): Directive[Unit] =
    path(splittingPathMatcher(modelPath, paramMatchers))
}