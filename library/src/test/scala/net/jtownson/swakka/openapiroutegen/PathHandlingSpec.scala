/*
 * Copyright 2017 Jeremy Townson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.jtownson.swakka.openapiroutegen

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{PathMatcher1, Route}
import akka.http.scaladsl.testkit.{RouteTest, TestFrameworkInterface}
import net.jtownson.swakka.openapiroutegen.ConvertibleToDirective.BooleanSegment
import net.jtownson.swakka.openapiroutegen.PathHandling.containsParamToken
import org.scalatest.FlatSpec
import org.scalatest.Matchers._
import org.scalatest.exceptions.TestFailedException

class PathHandlingSpec extends FlatSpec with RouteTest with TestFrameworkInterface {

  "PathWithParamMatcher" should "work for multiple params" in {

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
