package net.jtownson.swakka.openapiroutegen

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directive1
import akka.http.scaladsl.server.Directives.complete
import net.jtownson.swakka.openapimodel._
import org.scalatest.FlatSpec
import org.scalatest.Matchers._
import shapeless.HNil

class PathParamConstrainedConvertersSpec extends FlatSpec with ConverterTest {

  def voidConstraints[T] = Constraints[T]()

  // NB: according to the open api schema def, path parameters must have required = true.
  // i.e. Option types not supported.
  "PathParamConverters" should "convert a string path parameter" in {
    converterTest[String, PathParameterConstrained[String, String]](get("/a/x"), PathParameterConstrained[String, String](name = 'p, constraints = voidConstraints), "/a/{p}", "x")
  }

  they should "convert a float path parameter" in {
    converterTest[Float, PathParameterConstrained[Float, Float]](get("/a/3.14"), PathParameterConstrained[Float, Float](name = 'p, constraints = voidConstraints), "/a/{p}", "3.14")
  }

  they should "convert a double path parameter" in {
    converterTest[Double, PathParameterConstrained[Double, Double]](get("/a/3.14"), PathParameterConstrained[Double, Double](name = 'p, constraints = voidConstraints), "/a/{p}", "3.14")
  }

  they should "convert a boolean path parameter" in {
    converterTest[Boolean, PathParameterConstrained[Boolean, Boolean]](get("/a/true"), PathParameterConstrained[Boolean, Boolean](name = 'p, constraints = voidConstraints), "/a/{p}", "true")
  }

  they should "convert an int path parameter" in {
    converterTest[Int, PathParameterConstrained[Int, Int]](get("/a/2"), PathParameterConstrained[Int, Int](name = 'p, constraints = voidConstraints), "/a/{p}", "2")
  }

  they should "convert a long path parameter" in {
    converterTest[Long, PathParameterConstrained[Long, Long]](get("/a/2"), PathParameterConstrained[Long, Long](name = 'p, constraints = voidConstraints), "/a/{p}", "2")
  }

  they should "pass string path parameters iff validation succeeds" in {
    val pp = PathParameterConstrained[String, String]('pp, None, None, Constraints(enum = Some(Set("value1", "value2", "value3"))))
    converterTest[String, PathParameterConstrained[String, String]](Get("http://localhost/value1"), pp, OK, "/{pp}")
    converterTest[String, PathParameterConstrained[String, String]](Get("http://localhost/NoNoNo"), pp, BadRequest, "/{pp}")
  }

  they should "pass boolean path parameters iff validation succeeds" in {
    val pp = PathParameterConstrained[Boolean, Boolean]('pp, None, None, Constraints(enum = Some(Set(false))))
    converterTest[Boolean, PathParameterConstrained[Boolean, Boolean]](Get("http://localhost/false"), pp, OK, "/{pp}")
    converterTest[Boolean, PathParameterConstrained[Boolean, Boolean]](Get("http://localhost/true"), pp, BadRequest, "/{pp}")
  }

  they should "pass int path parameters iff validation succeeds" in {
    val pp = PathParameterConstrained[Int, Int]('pp, None, None, Constraints(enum = Some(Set(1, 2, 3))))
    converterTest[Int, PathParameterConstrained[Int, Int]](Get("http://localhost/3"), pp, OK, "/{pp}")
    converterTest[Int, PathParameterConstrained[Int, Int]](Get("http://localhost/4"), pp, BadRequest, "/{pp}")
  }

  they should "pass long path parameters iff validation succeeds" in {
    val pp = PathParameterConstrained[Long, Long]('pp, None, None, Constraints(enum = Some(Set(1, 2, 3))))
    converterTest[Long, PathParameterConstrained[Long, Long]](Get("http://localhost/3"), pp, OK, "/{pp}")
    converterTest[Long, PathParameterConstrained[Long, Long]](Get("http://localhost/4"), pp, BadRequest, "/{pp}")
  }

  they should "pass float path parameters iff validation succeeds" in {
    val pp = PathParameterConstrained[Float, Float]('pp, None, None, Constraints(enum = Some(Set(1))))
    converterTest[Float, PathParameterConstrained[Float, Float]](Get(s"http://localhost/1"), pp, OK, "/{pp}")
    converterTest[Float, PathParameterConstrained[Float, Float]](Get("http://localhost/4"), pp, BadRequest, "/{pp}")
  }

  they should "pass double path parameters iff validation succeeds" in {
    val pp = PathParameterConstrained[Double, Double]('pp, None, None, Constraints(enum = Some(Set(1))))
    converterTest[Double, PathParameterConstrained[Double, Double]](Get(s"http://localhost/1"), pp, OK, "/{pp}")
    converterTest[Double, PathParameterConstrained[Double, Double]](Get("http://localhost/4"), pp, BadRequest, "/{pp}")
  }

  they should "pass paths with parameter tokens" in {

    val toDirective: Directive1[HNil] = hNilConverter.convertToDirective("/a/{b}", HNil)

    val route = toDirective { _ =>
      complete("matched")
    }

    Get("http://localhost/x/y") ~> route ~> check {
      status shouldBe OK
      responseAs[String] shouldBe "matched"
    }
  }
}
