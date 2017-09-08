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
  (implicit ev: ConvertibleToDirective[U]): Unit = {
    request ~> seal(route[T, U]("", param)) ~> check {
      status shouldBe expectedStatus
    }
  }

  def route[T, U <: Parameter[T]](modelPath: String, param: U)
                                         (implicit ev: ConvertibleToDirective[U]): Route = {
    converter(param)(ev).convertToDirective(modelPath, param) {
      qpc => complete(qpc.value.toString)
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
