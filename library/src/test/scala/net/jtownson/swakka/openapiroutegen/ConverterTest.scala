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

import akka.http.scaladsl.model.StatusCodes.OK
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Route.seal
import akka.http.scaladsl.testkit.{RouteTest, TestFrameworkInterface}
import net.jtownson.swakka.openapimodel._
import net.jtownson.swakka.openapiroutegen.OpenApiDirective.converter
import org.scalatest.Assertion
import org.scalatest.Matchers._

import scala.reflect.ClassTag

trait ConverterTest extends RouteTest with TestFrameworkInterface {

  def converterTest[T, U <: Parameter[T]]
  (request: HttpRequest, param: U, expectedResponse: String)
  (implicit ev: OpenApiDirective[U]): Unit = {
    converterTest(request, route[T, U]("", param), expectedResponse)
  }

  def converterTest[T, U <: Parameter[T]]
  (request: HttpRequest, param: U, modelPath: String, expectedResponse: String)
  (implicit ev: OpenApiDirective[U]): Unit = {
    converterTest(request, route[T, U](modelPath, param), expectedResponse)
  }

  def converterTest[T, U <: Parameter[T]]
  (request: HttpRequest, route: Route, expectedResponse: String): Unit = {
    request ~> route ~> check {
      responseAs[String] shouldBe expectedResponse
    }
  }

  def converterTest[T, U <: Parameter[T]]
  (request: HttpRequest, param: U, expectedStatus: StatusCode)
  (implicit ev: OpenApiDirective[U]): Unit =
    converterTest[T, U](request, param, expectedStatus, "")

  def converterTest[T, U <: Parameter[T]]
  (request: HttpRequest, param: U, expectedStatus: StatusCode, modelPath: String)
  (implicit ev: OpenApiDirective[U]): Unit = {
    request ~> seal(route[T, U](modelPath, param)) ~> check {
      status shouldBe expectedStatus
    }
  }

  def converterTest[T: ClassTag, U <: Parameter[T]]
  (request: HttpRequest, param: U, expectedStatus: StatusCode, extractionTest: T => Assertion)
  (implicit ev: OpenApiDirective[U]): Unit = {
    val route = converter(param)(ev).convertToDirective("", param) {
      extraction => {
        extractionTest(extraction.value)
        complete(OK)
      }
    }
    request ~> seal(route) ~> check {
      status shouldBe expectedStatus
    }
  }

  def route[T, U <: Parameter[T]](modelPath: String, param: U)
                                         (implicit ev: OpenApiDirective[U]): Route = {
    converter(param)(ev).convertToDirective(modelPath, param) {
      (qpc: U) => complete(qpc.value.toString)
    }
  }

  def get(path: String, header: String, value: String): HttpRequest =
    get(path).withHeaders(List(RawHeader(header, value)))

  def post(path: String, body: String): HttpRequest =
    Post(s"http://example.com$path", HttpEntity(ContentTypes.`application/json`, body))

  def post(path: String): HttpRequest =
    Post(s"http://example.com$path")

  def get(path: String): HttpRequest =
    Get(s"http://example.com$path")

  def extractionAssertion[T](t: T): T => Assertion =
    _ shouldBe t

  case class Pet(id: Int, name: String)

  override def failTest(msg: String): Nothing = throw new AssertionError(msg)
}

object ConverterTest extends ConverterTest
