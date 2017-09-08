package net.jtownson.swakka.routegen

import net.jtownson.swakka.model.Parameters.HeaderParameter
import org.scalatest.FlatSpec

class HeaderParamConvertersSpec extends FlatSpec with ConverterTest {

  it should "convert a string header parameter" in {
    converterTest[String, HeaderParameter[String]](get("/", "x-p", "x"), "x", HeaderParameter[String](Symbol("x-p")))
    converterTest[Option[String], HeaderParameter[Option[String]]](get("/", "x-p", "x"), "Some(x)", HeaderParameter[Option[String]](Symbol("x-p")))
    converterTest[Option[String], HeaderParameter[Option[String]]](get("/"), "None", HeaderParameter[Option[String]](Symbol("x-p")))
    converterTest[String, HeaderParameter[String]](get("/"), "x", HeaderParameter[String](Symbol("x-p"), default = Some("x")))
    converterTest[Option[String], HeaderParameter[Option[String]]](get("/"), "Some(x)", HeaderParameter[Option[String]](Symbol("x-p"), default = Some(Some("x"))))
  }

  it should "convert a float header parameter" in {
    converterTest[Float, HeaderParameter[Float]](get("/", "x-p", "3.14"), "3.14", HeaderParameter[Float](Symbol("x-p")))
    converterTest[Option[Float], HeaderParameter[Option[Float]]](get("/", "x-p", "3.14"), "Some(3.14)", HeaderParameter[Option[Float]](Symbol("x-p")))
    converterTest[Option[Float], HeaderParameter[Option[Float]]](get("/"), "None", HeaderParameter[Option[Float]](Symbol("x-p")))
    converterTest[Float, HeaderParameter[Float]](get("/"), "3.14", HeaderParameter[Float](Symbol("x-p"), default = Some(3.14f)))
    converterTest[Option[Float], HeaderParameter[Option[Float]]](get("/"), "Some(3.14)", HeaderParameter[Option[Float]](Symbol("x-p"), default = Some(Some(3.14f))))
  }

  it should "convert a double header parameter" in {
    converterTest[Double, HeaderParameter[Double]](get("/", "x-p", "3.14"), "3.14", HeaderParameter[Double](Symbol("x-p")))
    converterTest[Option[Double], HeaderParameter[Option[Double]]](get("/", "x-p", "3.14"), "Some(3.14)", HeaderParameter[Option[Double]](Symbol("x-p")))
    converterTest[Option[Double], HeaderParameter[Option[Double]]](get("/"), "None", HeaderParameter[Option[Double]](Symbol("x-p")))
    converterTest[Double, HeaderParameter[Double]](get("/"), "3.14", HeaderParameter[Double](Symbol("x-p"), default = Some(3.14)))
    converterTest[Option[Double], HeaderParameter[Option[Double]]](get("/"), "Some(3.14)", HeaderParameter[Option[Double]](Symbol("x-p"), default = Some(Some(3.14))))
  }

  it should "convert a boolean header parameter" in {
    converterTest[Boolean, HeaderParameter[Boolean]](get("/", "x-p", "true"), "true", HeaderParameter[Boolean](Symbol("x-p")))
    converterTest[Option[Boolean], HeaderParameter[Option[Boolean]]](get("/", "x-p", "true"), "Some(true)", HeaderParameter[Option[Boolean]](Symbol("x-p")))
    converterTest[Option[Boolean], HeaderParameter[Option[Boolean]]](get("/"), "None", HeaderParameter[Option[Boolean]](Symbol("x-p")))
    converterTest[Boolean, HeaderParameter[Boolean]](get("/"), "true", HeaderParameter[Boolean](Symbol("x-p"), default = Some(true)))
    converterTest[Option[Boolean], HeaderParameter[Option[Boolean]]](get("/"), "Some(true)", HeaderParameter[Option[Boolean]](Symbol("x-p"), default = Some(Some(true))))
  }

  it should "convert a int header parameter" in {
    converterTest[Int, HeaderParameter[Int]](get("/", "x-p", "2"), "2", HeaderParameter[Int](Symbol("x-p")))
    converterTest[Option[Int], HeaderParameter[Option[Int]]](get("/", "x-p", "2"), "Some(2)", HeaderParameter[Option[Int]](Symbol("x-p")))
    converterTest[Option[Int], HeaderParameter[Option[Int]]](get("/"), "None", HeaderParameter[Option[Int]](Symbol("x-p")))
    converterTest[Int, HeaderParameter[Int]](get("/"), "2", HeaderParameter[Int](Symbol("x-p"), default = Some(2)))
    converterTest[Option[Int], HeaderParameter[Option[Int]]](get("/"), "Some(2)", HeaderParameter[Option[Int]](Symbol("x-p"), default = Some(Some(2))))
  }

  it should "convert a long header parameter" in {
    converterTest[Long, HeaderParameter[Long]](get("/", "x-p", "2"), "2", HeaderParameter[Long](Symbol("x-p")))
    converterTest[Option[Long], HeaderParameter[Option[Long]]](get("/", "x-p", "2"), "Some(2)", HeaderParameter[Option[Long]](Symbol("x-p")))
    converterTest[Option[Long], HeaderParameter[Option[Long]]](get("/"), "None", HeaderParameter[Option[Long]](Symbol("x-p")))
    converterTest[Long, HeaderParameter[Long]](get("/"), "2", HeaderParameter[Long](Symbol("x-p"), default = Some(2)))
    converterTest[Option[Long], HeaderParameter[Option[Long]]](get("/"), "Some(2)", HeaderParameter[Option[Long]](Symbol("x-p"), default = Some(Some(2))))
  }


}
