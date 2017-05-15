package net.jtownson.swakka.routegen

import akka.http.scaladsl.model.StatusCodes.{NotFound, OK}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.PathMatcher.{Matched, Matching, Unmatched}
import akka.http.scaladsl.server.{PathMatcher, PathMatcher0, PathMatcher1}
import akka.http.scaladsl.server.Route.seal
import akka.http.scaladsl.testkit.{RouteTest, TestFrameworkInterface}
import org.scalatest.FlatSpec
import org.scalatest.Matchers._
import org.scalatest.exceptions.TestFailedException

class PathHandlingSpec extends FlatSpec with RouteTest with TestFrameworkInterface {

  "PathHandling" should "match a path with a parameter" in {
    val modelPath = "/widgets/{widgetId}"

    val params: Map[String, PathMatcher[Unit]] = Map("{widgetId}" -> IntNumber.tmap(_ => ()))

    val route = PathHandling.akkaPath(modelPath, params) {
      complete("OK")
    }

    Get("http://foo.com/widgets/12345") ~> route ~> check {
      status shouldBe OK
    }

    Get("http://foo.com/widgets/bam") ~> seal(route) ~> check {
      status shouldBe NotFound
    }
  }

  "PathHandling" should "match my experiments" in {

    val matcher: PathMatcher[Tuple1[Int]] = Slash ~ PathMatcher("foo") / IntNumber / PathMatcher("bar")

    val request = Get("http://foo.com/foo/123/bar/baz")

    PathHandling.combine3()(request.uri.path) match {
      case Matched(p, Tuple1(i)) => println(s"ok, got $p, $i")
      case Unmatched => fail("did not match")
    }
  }

  def failTest(msg: String) = throw new TestFailedException(msg, 11)
}
