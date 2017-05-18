package net.jtownson.swakka.routegen

import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.{RouteTest, TestFrameworkInterface}
import net.jtownson.swakka.model.Parameters.QueryParameter
import net.jtownson.swakka.routegen.ConvertibleToDirective._
import org.scalatest.FlatSpec
import org.scalatest.Matchers._

class ConvertibleToDirectiveSpec extends FlatSpec with RouteTest with TestFrameworkInterface {

  "ConvertibleToDirective" should "convert a string query parameter" in {
    queryConverterTest(get("/path?q=x"), "x", QueryParameter[String]('q))
  }

  "ConvertibleToDirective" should "convert a float query parameter" in {
    queryConverterTest(get("/path?q=3.14"), "3.14", QueryParameter[Float]('q))
  }

  "ConvertibleToDirective" should "convert a double query parameter" in {
    queryConverterTest(get("/path?q=3.14"), "3.14", QueryParameter[Double]('q))
  }

  "ConvertibleToDirective" should "convert a boolean query parameter" in {
    queryConverterTest(get("/path?q=true"), "true", QueryParameter[Boolean]('q))
  }

  "ConvertibleToDirective" should "convert an int query parameter" in {
    queryConverterTest(get("/path?q=2"), "2", QueryParameter[Int]('q))
  }

  "ConvertibleToDirective" should "convert a long query parameter" in {
    queryConverterTest(get("/path?q=2"), "2", QueryParameter[Long]('q))
  }

  // TODO add path param and header param tests

  private def queryConverterTest[T](request: HttpRequest, expectedResponse: String, qp: QueryParameter[T])
                              (implicit ev: ConvertibleToDirective[QueryParameter[T]]): Unit = {
    request ~> route(qp) ~> check {
      responseAs[String] shouldBe expectedResponse
    }
  }

  private def route[T](qp: QueryParameter[T])
                      (implicit ev: ConvertibleToDirective[QueryParameter[T]]): Route = {
    converter(qp)(ev).convertToDirective("", qp) { qpc =>
      complete(qpc.value.toString)
    }
  }

  private def get(path: String): HttpRequest =
    Get(s"http://example.com/$path")

  override def failTest(msg: String): Nothing = throw new AssertionError(msg)
}
