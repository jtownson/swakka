package net.jtownson.swakka.routegen

import akka.http.scaladsl.model.StatusCodes.{BadRequest, OK}
import akka.http.scaladsl.model.headers.RawHeader
import net.jtownson.swakka.model.Parameters.HeaderParameter
import org.scalatest.FlatSpec

class HeaderParamConvertersSpec extends FlatSpec with ConverterTest {

  "HeaderParamConverters" should "convert a string header parameter" in {
    converterTest[String, HeaderParameter[String]](get("/", "x-p", "x"), "x", HeaderParameter[String](Symbol("x-p")))
    converterTest[Option[String], HeaderParameter[Option[String]]](get("/", "x-p", "x"), "Some(x)", HeaderParameter[Option[String]](Symbol("x-p")))
    converterTest[Option[String], HeaderParameter[Option[String]]](get("/"), "None", HeaderParameter[Option[String]](Symbol("x-p")))
    converterTest[String, HeaderParameter[String]](get("/"), "x", HeaderParameter[String](Symbol("x-p"), default = Some("x")))
    converterTest[Option[String], HeaderParameter[Option[String]]](get("/"), "Some(x)", HeaderParameter[Option[String]](Symbol("x-p"), default = Some(Some("x"))))
  }

  they should "convert a float header parameter" in {
    converterTest[Float, HeaderParameter[Float]](get("/", "x-p", "3.14"), "3.14", HeaderParameter[Float](Symbol("x-p")))
    converterTest[Option[Float], HeaderParameter[Option[Float]]](get("/", "x-p", "3.14"), "Some(3.14)", HeaderParameter[Option[Float]](Symbol("x-p")))
    converterTest[Option[Float], HeaderParameter[Option[Float]]](get("/"), "None", HeaderParameter[Option[Float]](Symbol("x-p")))
    converterTest[Float, HeaderParameter[Float]](get("/"), "3.14", HeaderParameter[Float](Symbol("x-p"), default = Some(3.14f)))
    converterTest[Option[Float], HeaderParameter[Option[Float]]](get("/"), "Some(3.14)", HeaderParameter[Option[Float]](Symbol("x-p"), default = Some(Some(3.14f))))
  }

  they should "convert a double header parameter" in {
    converterTest[Double, HeaderParameter[Double]](get("/", "x-p", "3.14"), "3.14", HeaderParameter[Double](Symbol("x-p")))
    converterTest[Option[Double], HeaderParameter[Option[Double]]](get("/", "x-p", "3.14"), "Some(3.14)", HeaderParameter[Option[Double]](Symbol("x-p")))
    converterTest[Option[Double], HeaderParameter[Option[Double]]](get("/"), "None", HeaderParameter[Option[Double]](Symbol("x-p")))
    converterTest[Double, HeaderParameter[Double]](get("/"), "3.14", HeaderParameter[Double](Symbol("x-p"), default = Some(3.14)))
    converterTest[Option[Double], HeaderParameter[Option[Double]]](get("/"), "Some(3.14)", HeaderParameter[Option[Double]](Symbol("x-p"), default = Some(Some(3.14))))
  }

  they should "convert a boolean header parameter" in {
    converterTest[Boolean, HeaderParameter[Boolean]](get("/", "x-p", "true"), "true", HeaderParameter[Boolean](Symbol("x-p")))
    converterTest[Option[Boolean], HeaderParameter[Option[Boolean]]](get("/", "x-p", "true"), "Some(true)", HeaderParameter[Option[Boolean]](Symbol("x-p")))
    converterTest[Option[Boolean], HeaderParameter[Option[Boolean]]](get("/"), "None", HeaderParameter[Option[Boolean]](Symbol("x-p")))
    converterTest[Boolean, HeaderParameter[Boolean]](get("/"), "true", HeaderParameter[Boolean](Symbol("x-p"), default = Some(true)))
    converterTest[Option[Boolean], HeaderParameter[Option[Boolean]]](get("/"), "Some(true)", HeaderParameter[Option[Boolean]](Symbol("x-p"), default = Some(Some(true))))
  }

  they should "convert a int header parameter" in {
    converterTest[Int, HeaderParameter[Int]](get("/", "x-p", "2"), "2", HeaderParameter[Int](Symbol("x-p")))
    converterTest[Option[Int], HeaderParameter[Option[Int]]](get("/", "x-p", "2"), "Some(2)", HeaderParameter[Option[Int]](Symbol("x-p")))
    converterTest[Option[Int], HeaderParameter[Option[Int]]](get("/"), "None", HeaderParameter[Option[Int]](Symbol("x-p")))
    converterTest[Int, HeaderParameter[Int]](get("/"), "2", HeaderParameter[Int](Symbol("x-p"), default = Some(2)))
    converterTest[Option[Int], HeaderParameter[Option[Int]]](get("/"), "Some(2)", HeaderParameter[Option[Int]](Symbol("x-p"), default = Some(Some(2))))
  }

  they should "convert a long header parameter" in {
    converterTest[Long, HeaderParameter[Long]](get("/", "x-p", "2"), "2", HeaderParameter[Long](Symbol("x-p")))
    converterTest[Option[Long], HeaderParameter[Option[Long]]](get("/", "x-p", "2"), "Some(2)", HeaderParameter[Option[Long]](Symbol("x-p")))
    converterTest[Option[Long], HeaderParameter[Option[Long]]](get("/"), "None", HeaderParameter[Option[Long]](Symbol("x-p")))
    converterTest[Long, HeaderParameter[Long]](get("/"), "2", HeaderParameter[Long](Symbol("x-p"), default = Some(2)))
    converterTest[Option[Long], HeaderParameter[Option[Long]]](get("/"), "Some(2)", HeaderParameter[Option[Long]](Symbol("x-p"), default = Some(Some(2))))
  }


  they should "pass enumerated string header parameters iff the request provides a valid enum value" in {
    val hp = HeaderParameter[String]('x, None, None, Some(Seq("value1", "value2", "value3")))
    converterTest[String, HeaderParameter[String]](Get("http://localhost").withHeaders(RawHeader("x", "value2")), hp, OK)
    converterTest[String, HeaderParameter[String]](Get("http://localhost").withHeaders(RawHeader("x", "nonono")), hp, BadRequest)
  }

  they should "pass enumerated optional string header parameters iff the request provides a valid enum value" in {
    val hp = HeaderParameter[Option[String]]('x, None, None, Some(Seq(Some("value1"), Some("value2"), Some("value3"))))
    converterTest[Option[String], HeaderParameter[Option[String]]](Get("http://localhost").withHeaders(RawHeader("x", "value2")), hp, OK)
    converterTest[Option[String], HeaderParameter[Option[String]]](Get("http://localhost").withHeaders(RawHeader("x", "nonono")), hp, BadRequest)
  }

  they should "pass enumerated boolean header parameters iff the request provides a valid enum value" in {
    val hp = HeaderParameter[Boolean]('x, None, None, Some(Seq(false)))
    converterTest[Boolean, HeaderParameter[Boolean]](Get("http://localhost").withHeaders(RawHeader("x", "false")), hp, OK)
    converterTest[Boolean, HeaderParameter[Boolean]](Get("http://localhost").withHeaders(RawHeader("x", "true")), hp, BadRequest)
  }

  they should "pass enumerated optional boolean header parameters iff the request provides a valid enum value" in {
    val hp = HeaderParameter[Option[Boolean]]('x, None, None, Some(Seq(Some(false))))
    converterTest[Option[Boolean], HeaderParameter[Option[Boolean]]](Get("http://localhost").withHeaders(RawHeader("x", "false")), hp, OK)
    converterTest[Option[Boolean], HeaderParameter[Option[Boolean]]](Get("http://localhost").withHeaders(RawHeader("x", "true")), hp, BadRequest)
  }

  they should "pass enumerated int header parameters iff the request provides a valid enum value" in {
    val hp = HeaderParameter[Int]('x, None, None, Some(Seq(1, 2, 3)))
    converterTest[Int, HeaderParameter[Int]](Get("http://localhost").withHeaders(RawHeader("x", "3")), hp, OK)
    converterTest[Int, HeaderParameter[Int]](Get("http://localhost").withHeaders(RawHeader("x", "4")), hp, BadRequest)
  }

  they should "pass enumerated optional int header parameters iff the request provides a valid enum value" in {
    val hp = HeaderParameter[Option[Int]]('x, None, None, Some(Seq(Some(1), Some(2), Some(3))))
    converterTest[Option[Int], HeaderParameter[Option[Int]]](Get("http://localhost").withHeaders(RawHeader("x", "3")), hp, OK)
    converterTest[Option[Int], HeaderParameter[Option[Int]]](Get("http://localhost").withHeaders(RawHeader("x", "4")), hp, BadRequest)
  }

  they should "pass enumerated long header parameters iff the request provides a valid enum value" in {
    val hp = HeaderParameter[Long]('x, None, None, Some(Seq(1, 2, 3)))
    converterTest[Long, HeaderParameter[Long]](Get("http://localhost").withHeaders(RawHeader("x", "3")), hp, OK)
    converterTest[Long, HeaderParameter[Long]](Get("http://localhost").withHeaders(RawHeader("x", "4")), hp, BadRequest)
  }

  they should "pass enumerated optional long header parameters iff the request provides a valid enum value" in {
    val hp = HeaderParameter[Option[Long]]('x, None, None, Some(Seq(Some(1), Some(2), Some(3))))
    converterTest[Option[Long], HeaderParameter[Option[Long]]](Get("http://localhost").withHeaders(RawHeader("x", "3")), hp, OK)
    converterTest[Option[Long], HeaderParameter[Option[Long]]](Get("http://localhost").withHeaders(RawHeader("x", "4")), hp, BadRequest)
  }

  they should "pass enumerated float header parameters iff the request provides a valid enum value" in {
    val hp = HeaderParameter[Float]('x, None, None, Some(Seq(Float.MaxValue)))
    converterTest[Float, HeaderParameter[Float]](Get("http://localhost").withHeaders(RawHeader("x", Float.MaxValue.toString)), hp, OK)
    converterTest[Float, HeaderParameter[Float]](Get("http://localhost").withHeaders(RawHeader("x", "1")), hp, BadRequest)
  }

  they should "pass enumerated optional float header parameters iff the request provides a valid enum value" in {
    val hp = HeaderParameter[Option[Float]]('x, None, None, Some(Seq(Some(Float.MaxValue))))
    converterTest[Option[Float], HeaderParameter[Option[Float]]](Get("http://localhost").withHeaders(RawHeader("x", Float.MaxValue.toString)), hp, OK)
    converterTest[Option[Float], HeaderParameter[Option[Float]]](Get("http://localhost").withHeaders(RawHeader("x", "1")), hp, BadRequest)
  }

  they should "pass enumerated double header parameters iff the request provides a valid enum value" in {
    val hp = HeaderParameter[Double]('x, None, None, Some(Seq(Double.MaxValue)))
    converterTest[Double, HeaderParameter[Double]](Get("http://localhost").withHeaders(RawHeader("x", Double.MaxValue.toString)), hp, OK)
    converterTest[Double, HeaderParameter[Double]](Get("http://localhost").withHeaders(RawHeader("x", "1")), hp, BadRequest)
  }

  they should "pass enumerated optional double header parameters iff the request provides a valid enum value" in {
    val hp = HeaderParameter[Option[Double]]('x, None, None, Some(Seq(Some(Double.MaxValue))))
    converterTest[Option[Double], HeaderParameter[Option[Double]]](Get("http://localhost").withHeaders(RawHeader("x", Double.MaxValue.toString)), hp, OK)
    converterTest[Option[Double], HeaderParameter[Option[Double]]](Get("http://localhost").withHeaders(RawHeader("x", "1")), hp, BadRequest)
  }
}
