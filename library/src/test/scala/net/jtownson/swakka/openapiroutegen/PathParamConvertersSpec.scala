package net.jtownson.swakka.openapiroutegen

import akka.http.scaladsl.server.Directive1
import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.model.StatusCodes._
import net.jtownson.swakka.openapimodel._
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
