package net.jtownson.swakka.routegen

import akka.http.scaladsl.model.StatusCodes.{NotFound, OK}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{PathMatcher, PathMatcher1, Route}
import akka.http.scaladsl.server.Route.seal
import akka.http.scaladsl.testkit.{RouteTest, TestFrameworkInterface}
import net.jtownson.swakka.routegen.ConvertibleToDirective.BooleanSegment
import net.jtownson.swakka.routegen.PathHandling.containsParamToken
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

  "pathWithParamMatcher" should "work for multiple params" in {

    val modelPath = "/foo/{param1}/bar/{param2}"

    val request = Get("http://foo.com/foo/123/bar/true")

    val pm1: PathMatcher1[Int] = PathHandling.pathWithParamMatcher(modelPath, "param1", IntNumber)
    val pm2: PathMatcher1[Boolean] = PathHandling.pathWithParamMatcher(modelPath, "param2", BooleanSegment)

    val route: Route = rawPathPrefixTest(pm1) { ip =>
      rawPathPrefixTest(pm2) { bp =>
        complete(s"Okay: $ip, $bp")
      }
    }

    request ~> route ~> check {
      responseAs[String] shouldBe "Okay: 123, true"
    }
  }

  "containsParamToken" should "get the right answer" in {

    containsParamToken("/{a}/b") shouldBe true
    containsParamToken("/a/b") shouldBe false
    containsParamToken("a/{ b }") shouldBe true
  }

  def failTest(msg: String) = throw new TestFailedException(msg, 11)
}
