package net.jtownson.swakka.routegen

import akka.http.scaladsl.model.StatusCodes.{NotFound, OK}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.PathMatcher
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

  def failTest(msg: String) = throw new TestFailedException(msg, 11)
}
