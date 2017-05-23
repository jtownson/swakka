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
    converterTest[Option[String], QueryParameter[Option[String]]](get("/path?q=x"), "Some(x)", QueryParameter[Option[String]]('q))
    converterTest[Option[String], QueryParameter[Option[String]]](get("/path"), "None", QueryParameter[Option[String]]('q))
  }

  it should "convert a float query parameter" in {
    converterTest[Float, QueryParameter[Float]](get("/path?q=3.14"), "3.14", QueryParameter[Float]('q))
    converterTest[Option[Float], QueryParameter[Option[Float]]](get("/path?q=3.14"), "Some(3.14)", QueryParameter[Option[Float]]('q))
    converterTest[Option[Float], QueryParameter[Option[Float]]](get("/path"), "None", QueryParameter[Option[Float]]('q))
  }

  it should "convert a double query parameter" in {
    converterTest[Double, QueryParameter[Double]](get("/path?q=3.14"), "3.14", QueryParameter[Double]('q))
    converterTest[Option[Double], QueryParameter[Option[Double]]](get("/path?q=3.14"), "Some(3.14)", QueryParameter[Option[Double]]('q))
    converterTest[Option[Double], QueryParameter[Option[Double]]](get("/path"), "None", QueryParameter[Option[Double]]('q))
  }

  it should "convert a boolean query parameter" in {
    converterTest[Boolean, QueryParameter[Boolean]](get("/path?q=true"), "true", QueryParameter[Boolean]('q))
    converterTest[Option[Boolean], QueryParameter[Option[Boolean]]](get("/path?q=true"), "Some(true)", QueryParameter[Option[Boolean]]('q))
    converterTest[Option[Boolean], QueryParameter[Option[Boolean]]](get("/path"), "None", QueryParameter[Option[Boolean]]('q))
  }

  it should "convert an int query parameter" in {
    converterTest[Int, QueryParameter[Int]](get("/path?q=2"), "2", QueryParameter[Int]('q))
    converterTest[Option[Int], QueryParameter[Option[Int]]](get("/path?q=2"), "Some(2)", QueryParameter[Option[Int]]('q))
    converterTest[Option[Int], QueryParameter[Option[Int]]](get("/path"), "None", QueryParameter[Option[Int]]('q))
  }

  it should "convert a long query parameter" in {
    converterTest[Long, QueryParameter[Long]](get("/path?q=2"), "2", QueryParameter[Long]('q))
    converterTest[Option[Long], QueryParameter[Option[Long]]](get("/path?q=2"), "Some(2)", QueryParameter[Option[Long]]('q))
    converterTest[Option[Long], QueryParameter[Option[Long]]](get("/path"), "None", QueryParameter[Option[Long]]('q))
  }

  it should "convert a string header parameter" in {
    converterTest[String, HeaderParameter[String]](get("/", "x-p", "x"), "x", HeaderParameter[String](Symbol("x-p")))
    converterTest[Option[String], HeaderParameter[Option[String]]](get("/", "x-p", "x"), "Some(x)", HeaderParameter[Option[String]](Symbol("x-p")))
    converterTest[Option[String], HeaderParameter[Option[String]]](get("/"), "None", HeaderParameter[Option[String]](Symbol("x-p")))
  }

  it should "convert a float header parameter" in {
    converterTest[Float, HeaderParameter[Float]](get("/", "x-p", "3.14"), "3.14", HeaderParameter[Float](Symbol("x-p")))
    converterTest[Option[Float], HeaderParameter[Option[Float]]](get("/", "x-p", "3.14"), "Some(3.14)", HeaderParameter[Option[Float]](Symbol("x-p")))
    converterTest[Option[Float], HeaderParameter[Option[Float]]](get("/"), "None", HeaderParameter[Option[Float]](Symbol("x-p")))
  }

  it should "convert a double header parameter" in {
    converterTest[Double, HeaderParameter[Double]](get("/", "x-p", "3.14"), "3.14", HeaderParameter[Double](Symbol("x-p")))
    converterTest[Option[Double], HeaderParameter[Option[Double]]](get("/", "x-p", "3.14"), "Some(3.14)", HeaderParameter[Option[Double]](Symbol("x-p")))
    converterTest[Option[Double], HeaderParameter[Option[Double]]](get("/"), "None", HeaderParameter[Option[Double]](Symbol("x-p")))
  }

  it should "convert a boolean header parameter" in {
    converterTest[Boolean, HeaderParameter[Boolean]](get("/", "x-p", "true"), "true", HeaderParameter[Boolean](Symbol("x-p")))
    converterTest[Option[Boolean], HeaderParameter[Option[Boolean]]](get("/", "x-p", "true"), "Some(true)", HeaderParameter[Option[Boolean]](Symbol("x-p")))
    converterTest[Option[Boolean], HeaderParameter[Option[Boolean]]](get("/"), "None", HeaderParameter[Option[Boolean]](Symbol("x-p")))
  }

  it should "convert a int header parameter" in {
    converterTest[Int, HeaderParameter[Int]](get("/", "x-p", "2"), "2", HeaderParameter[Int](Symbol("x-p")))
    converterTest[Option[Int], HeaderParameter[Option[Int]]](get("/", "x-p", "2"), "Some(2)", HeaderParameter[Option[Int]](Symbol("x-p")))
    converterTest[Option[Int], HeaderParameter[Option[Int]]](get("/"), "None", HeaderParameter[Option[Int]](Symbol("x-p")))
  }

  it should "convert a long header parameter" in {
    converterTest[Long, HeaderParameter[Long]](get("/", "x-p", "2"), "2", HeaderParameter[Long](Symbol("x-p")))
    converterTest[Option[Long], HeaderParameter[Option[Long]]](get("/", "x-p", "2"), "Some(2)", HeaderParameter[Option[Long]](Symbol("x-p")))
    converterTest[Option[Long], HeaderParameter[Option[Long]]](get("/"), "None", HeaderParameter[Option[Long]](Symbol("x-p")))
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
    request ~> route[T, U](param)(ev) ~> check {
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
