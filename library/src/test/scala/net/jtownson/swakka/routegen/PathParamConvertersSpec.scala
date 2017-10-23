package net.jtownson.swakka.routegen

import akka.http.scaladsl.server.Directive1
import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.model.StatusCodes._
import net.jtownson.swakka.model.Parameters.PathParameter
import net.jtownson.swakka.routegen.ConvertibleToDirective.hNilConverter
import org.scalatest.FlatSpec
import org.scalatest.Matchers._
import shapeless.HNil

class PathParamConvertersSpec extends FlatSpec with ConverterTest {

  // NB: according to the open api schema def, path parameters must have required = true.
  // i.e. Option types not supported.
  "PathParamConverters" should "convert a string path parameter" in {
    converterTest[String, PathParameter[String]](get("/a/x"), PathParameter[String]('p), "/a/{p}", "x")
  }

  they should "convert a float path parameter" in {
    converterTest[Float, PathParameter[Float]](get("/a/3.14"), PathParameter[Float]('p), "/a/{p}", "3.14")
  }

  they should "convert a double path parameter" in {
    converterTest[Double, PathParameter[Double]](get("/a/3.14"), PathParameter[Double]('p), "/a/{p}", "3.14")
  }

  they should "convert a boolean path parameter" in {
    converterTest[Boolean, PathParameter[Boolean]](get("/a/true"), PathParameter[Boolean]('p), "/a/{p}", "true")
  }

  they should "convert an int path parameter" in {
    converterTest[Int, PathParameter[Int]](get("/a/2"), PathParameter[Int]('p), "/a/{p}", "2")
  }

  they should "convert a long path parameter" in {
    converterTest[Long, PathParameter[Long]](get("/a/2"), PathParameter[Long]('p), "/a/{p}", "2")
  }

  they should "pass enumerated string path parameters iff the request provides a valid enum value" in {
    val pp = PathParameter[String]('pp, None, None, Some(Seq("value1", "value2", "value3")))
    converterTest[String, PathParameter[String]](Get("http://localhost/value1"), pp, OK, "/{pp}")
    converterTest[String, PathParameter[String]](Get("http://localhost/NoNoNo"), pp, BadRequest, "/{pp}")
  }

  they should "pass enumerated boolean path parameters iff the request provides a valid enum value" in {
    val pp = PathParameter[Boolean]('pp, None, None, Some(Seq(false)))
    converterTest[Boolean, PathParameter[Boolean]](Get("http://localhost/false"), pp, OK, "/{pp}")
    converterTest[Boolean, PathParameter[Boolean]](Get("http://localhost/true"), pp, BadRequest, "/{pp}")
  }

  they should "pass enumerated int path parameters iff the request provides a valid enum value" in {
    val pp = PathParameter[Int]('pp, None, None, Some(Seq(1, 2, 3)))
    converterTest[Int, PathParameter[Int]](Get("http://localhost/3"), pp, OK, "/{pp}")
    converterTest[Int, PathParameter[Int]](Get("http://localhost/4"), pp, BadRequest, "/{pp}")
  }

  they should "pass enumerated long path parameters iff the request provides a valid enum value" in {
    val pp = PathParameter[Long]('pp, None, None, Some(Seq(1, 2, 3)))
    converterTest[Long, PathParameter[Long]](Get("http://localhost/3"), pp, OK, "/{pp}")
    converterTest[Long, PathParameter[Long]](Get("http://localhost/4"), pp, BadRequest, "/{pp}")
  }

  they should "pass enumerated float path parameters iff the request provides a valid enum value" in {
    val pp = PathParameter[Float]('pp, None, None, Some(Seq(1)))
    converterTest[Float, PathParameter[Float]](Get(s"http://localhost/1"), pp, OK, "/{pp}")
    converterTest[Float, PathParameter[Float]](Get("http://localhost/4"), pp, BadRequest, "/{pp}")
  }

  they should "pass enumerated double path parameters iff the request provides a valid enum value" in {
    val pp = PathParameter[Double]('pp, None, None, Some(Seq(1)))
    converterTest[Double, PathParameter[Double]](Get(s"http://localhost/1"), pp, OK, "/{pp}")
    converterTest[Double, PathParameter[Double]](Get("http://localhost/4"), pp, BadRequest, "/{pp}")
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
