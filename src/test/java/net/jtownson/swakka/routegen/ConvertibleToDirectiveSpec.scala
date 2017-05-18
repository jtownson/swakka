package net.jtownson.swakka.routegen

import akka.http.scaladsl.model.StatusCodes.{NotFound, OK}
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route.seal
import akka.http.scaladsl.server.{Directive1, Route}
import akka.http.scaladsl.testkit.{RouteTest, TestFrameworkInterface}
import net.jtownson.swakka.model.Parameters.{HeaderParameter, Parameter, QueryParameter}
import net.jtownson.swakka.routegen.ConvertibleToDirective._
import org.scalatest.FlatSpec
import org.scalatest.Matchers._
import shapeless.HNil

class ConvertibleToDirectiveSpec extends FlatSpec with RouteTest with TestFrameworkInterface {

  "ConvertibleToDirective" should "convert a string query parameter" in {
    converterTest[String, QueryParameter[String]](get("/path?q=x"), "x", QueryParameter[String]('q))
  }

  it should "convert a float query parameter" in {
    converterTest[Float, QueryParameter[Float]](get("/path?q=3.14"), "3.14", QueryParameter[Float]('q))
  }

  it should "convert a double query parameter" in {
    converterTest[Double, QueryParameter[Double]](get("/path?q=3.14"), "3.14", QueryParameter[Double]('q))
  }

  it should "convert a boolean query parameter" in {
    converterTest[Boolean, QueryParameter[Boolean]](get("/path?q=true"), "true", QueryParameter[Boolean]('q))
  }

  it should "convert an int query parameter" in {
    converterTest[Int, QueryParameter[Int]](get("/path?q=2"), "2", QueryParameter[Int]('q))
  }

  it should "convert a long query parameter" in {
    converterTest[Long, QueryParameter[Long]](get("/path?q=2"), "2", QueryParameter[Long]('q))
  }

  it should "convert a string header parameter" in {
    converterTest[String, HeaderParameter[String]](get("/", "x-p", "2"), "2", HeaderParameter[String](Symbol("x-p")))
  }

  it should "convert a float header parameter" in {
    converterTest[Float, HeaderParameter[Float]](get("/", "x-p", "2.1"), "2.1", HeaderParameter[Float](Symbol("x-p")))
  }

  it should "convert a double header parameter" in {
    converterTest[Double, HeaderParameter[Double]](get("/", "x-p", "2.1"), "2.1", HeaderParameter[Double](Symbol("x-p")))
  }

  it should "convert a boolean header parameter" in {
    converterTest[Boolean, HeaderParameter[Boolean]](get("/", "x-p", "true"), "true", HeaderParameter[Boolean](Symbol("x-p")))
  }

  it should "convert a int header parameter" in {
    converterTest[Int, HeaderParameter[Int]](get("/", "x-p", "2"), "2", HeaderParameter[Int](Symbol("x-p")))
  }

  it should "convert a long header parameter" in {
    converterTest[Long, HeaderParameter[Long]](get("/", "x-p", "2"), "2", HeaderParameter[Long](Symbol("x-p")))
  }


  // TODO add path, body param tests


  "HNil converter" should "match paths without parameter tokens" in {

    val toDirective: Directive1[HNil] = hNilConverter.convertToDirective("/a/b", HNil)

    val route = toDirective { _ =>
      complete("matched")
    }

    Get("http://localhost/a/b") ~> route ~> check {
      status shouldBe OK
      responseAs[String] shouldBe "matched"
    }

    Get("http://localhost/x/y") ~> seal(route) ~> check {
      status shouldBe NotFound
    }
  }

  it should "pass paths with parameter tokens" in {

    val toDirective: Directive1[HNil] = hNilConverter.convertToDirective("/a/{b}", HNil)

    val route = toDirective { _ =>
      complete("matched")
    }

    Get("http://localhost/x/y") ~> route ~> check {
      status shouldBe OK
      responseAs[String] shouldBe "matched"
    }
  }

  private def converterTest[T, U <: Parameter[T]](request: HttpRequest, expectedResponse: String, param: U)
                              (implicit ev: ConvertibleToDirective[U]): Unit = {
    request ~> route[T, U](param) ~> check {
      responseAs[String] shouldBe expectedResponse
    }
  }

  private def route[T, U <: Parameter[T]](param: U)
                      (implicit ev: ConvertibleToDirective[U]): Route = {
    converter(param)(ev).convertToDirective("", param) { qpc =>
      complete(qpc.value.toString)
    }
  }

  private def get(path: String, header: String, value: String): HttpRequest =
    get(path).withHeaders(List(RawHeader(header, value)))


  private def get(path: String): HttpRequest =
    Get(s"http://example.com/$path")

  override def failTest(msg: String): Nothing = throw new AssertionError(msg)
}
