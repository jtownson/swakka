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

package net.jtownson.swakka.routegen

import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpRequest, StatusCode}
import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Route.seal
import akka.http.scaladsl.testkit.{RouteTest, TestFrameworkInterface}
import net.jtownson.swakka.model.Parameters.Parameter
import net.jtownson.swakka.routegen.ConvertibleToDirective.converter
import org.scalatest.Matchers._

trait ConverterTest extends RouteTest with TestFrameworkInterface {

  def converterTest[T, U <: Parameter[T]]
  (request: HttpRequest, expectedResponse: String, param: U, modelPath: String = "")
  (implicit ev: ConvertibleToDirective[U]): Unit = {
    converterTest(request, expectedResponse, route[T, U](modelPath, param))
  }

  def converterTest[T, U <: Parameter[T]]
  (request: HttpRequest, expectedResponse: String, route: Route): Unit = {
    request ~> route ~> check {
      responseAs[String] shouldBe expectedResponse
    }
  }

  def converterTest[T, U <: Parameter[T]]
  (request: HttpRequest, param: U, expectedStatus: StatusCode)
  (implicit ev: ConvertibleToDirective[U]): Unit =
    converterTest[T, U](request, param, expectedStatus, "")

  def converterTest[T, U <: Parameter[T]]
  (request: HttpRequest, param: U, expectedStatus: StatusCode, modelPath: String)
  (implicit ev: ConvertibleToDirective[U]): Unit = {
    request ~> seal(route[T, U](modelPath, param)) ~> check {
      status shouldBe expectedStatus
    }
  }

  def converterTest[T, U <: Parameter[T]]
  (request: HttpRequest, param: U, expectedStatus: StatusCode, expectedResponse: T, modelPath: String)
  (implicit ev: ConvertibleToDirective[U]): Unit = {
    val route = converter(param)(ev).convertToDirective(modelPath, param) {
      extraction => complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, extraction.value.toString))
    }
    request ~> seal(route) ~> check {
      status shouldBe expectedStatus
    }
  }

  def route[T, U <: Parameter[T]](modelPath: String, param: U)
                                         (implicit ev: ConvertibleToDirective[U]): Route = {
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

  case class Pet(id: Int, name: String)

  override def failTest(msg: String): Nothing = throw new AssertionError(msg)
}

object ConverterTest extends ConverterTest
