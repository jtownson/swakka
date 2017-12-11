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

package net.jtownson.swakka.openapiroutegen

import akka.http.scaladsl.model.StatusCodes.{BadRequest, NotFound, OK}
import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.Route.seal
import net.jtownson.swakka.openapimodel._
import org.scalatest.FlatSpec
import org.scalatest.Matchers._

class QueryParamConvertersSpec extends FlatSpec with ConverterTest {

  "QueryParamConverters" should "convert a string query parameter" in {
    converterTest[String, QueryParameter[String]](get("/path?q=x"), QueryParameter[String]('q), "x")
    converterTest[Option[String], QueryParameter[Option[String]]](get("/path?q=x"), QueryParameter[Option[String]]('q), "Some(x)")
    converterTest[Option[String], QueryParameter[Option[String]]](get("/path"), QueryParameter[Option[String]]('q), "None")
    converterTest[String, QueryParameter[String]](get("/path"), QueryParameter[String]('p, default = Some("x")), "x")
    converterTest[Option[String], QueryParameter[Option[String]]](get("/path"), QueryParameter[Option[String]]('p, default = Some(Some("x"))), "Some(x)")
  }

  they should "convert a float query parameter" in {
    converterTest[Float, QueryParameter[Float]](get("/path?q=3.14"), QueryParameter[Float]('q), "3.14")
    converterTest[Option[Float], QueryParameter[Option[Float]]](get("/path?q=3.14"), QueryParameter[Option[Float]]('q), "Some(3.14)")
    converterTest[Option[Float], QueryParameter[Option[Float]]](get("/path"), QueryParameter[Option[Float]]('q), "None")
    converterTest[Float, QueryParameter[Float]](get("/path"), QueryParameter[Float]('p, default = Some(3.14f)), "3.14")
    converterTest[Option[Float], QueryParameter[Option[Float]]](get("/path"), QueryParameter[Option[Float]]('p, default = Some(Some(3.14f))), "Some(3.14)")
  }

  they should "convert a double query parameter" in {
    converterTest[Double, QueryParameter[Double]](get("/path?q=3.14"), QueryParameter[Double]('q), "3.14")
    converterTest[Option[Double], QueryParameter[Option[Double]]](get("/path?q=3.14"), QueryParameter[Option[Double]]('q), "Some(3.14)")
    converterTest[Option[Double], QueryParameter[Option[Double]]](get("/path"), QueryParameter[Option[Double]]('q), "None")
    converterTest[Double, QueryParameter[Double]](get("/path"), QueryParameter[Double]('p, default = Some(3.14)), "3.14")
    converterTest[Option[Double], QueryParameter[Option[Double]]](get("/path"), QueryParameter[Option[Double]]('p, default = Some(Some(3.14))), "Some(3.14)")
  }

  they should "convert a boolean query parameter" in {
    converterTest[Boolean, QueryParameter[Boolean]](get("/path?q=true"), QueryParameter[Boolean]('q), "true")
    converterTest[Option[Boolean], QueryParameter[Option[Boolean]]](get("/path?q=true"), QueryParameter[Option[Boolean]]('q), "Some(true)")
    converterTest[Option[Boolean], QueryParameter[Option[Boolean]]](get("/path"), QueryParameter[Option[Boolean]]('q), "None")
    converterTest[Boolean, QueryParameter[Boolean]](get("/path"), QueryParameter[Boolean]('p, default = Some(true)), "true")
    converterTest[Option[Boolean], QueryParameter[Option[Boolean]]](get("/path"), QueryParameter[Option[Boolean]]('p, default = Some(Some(true))), "Some(true)")
  }

  they should "convert an int query parameter" in {
    converterTest[Int, QueryParameter[Int]](get("/path?q=2"), QueryParameter[Int]('q), "2")
    converterTest[Option[Int], QueryParameter[Option[Int]]](get("/path?q=2"), QueryParameter[Option[Int]]('q), "Some(2)")
    converterTest[Option[Int], QueryParameter[Option[Int]]](get("/path"), QueryParameter[Option[Int]]('q), "None")
    converterTest[Int, QueryParameter[Int]](get("/path"), QueryParameter[Int]('p, default = Some(2)), "2")
    converterTest[Option[Int], QueryParameter[Option[Int]]](get("/path"), QueryParameter[Option[Int]]('p, default = Some(Some(2))), "Some(2)")
  }

  they should "convert a long query parameter" in {
    converterTest[Long, QueryParameter[Long]](get("/path?q=2"), QueryParameter[Long]('q), "2")
    converterTest[Option[Long], QueryParameter[Option[Long]]](get("/path?q=2"), QueryParameter[Option[Long]]('q), "Some(2)")
    converterTest[Option[Long], QueryParameter[Option[Long]]](get("/path"), QueryParameter[Option[Long]]('q), "None")
    converterTest[Long, QueryParameter[Long]](get("/path"), QueryParameter[Long]('p, default = Some(2)), "2")
    converterTest[Option[Long], QueryParameter[Option[Long]]](get("/path"), QueryParameter[Option[Long]]('p, default = Some(Some(2))), "Some(2)")
  }

  they should "pass QueryParameters iff their case is correct" in {

    val route = stringReqQueryConverter.convertToDirective("/", QueryParameter('q)) { _ =>
      complete("ok")
    }

    Get("http://localhost/?Q") ~> seal(route) ~> check {
      status shouldBe NotFound
    }
  }

  they should "pass enumerated string query parameters iff the request provides a valid enum value" in {
    val qp = QueryParameter[String]('qp, None, None, Some(Seq("value1", "value2", "value3")))
    converterTest[String, QueryParameter[String]](Get("http://localhost?qp=value1"), qp, OK)
    converterTest[String, QueryParameter[String]](Get("http://localhost?qp=NoNoNo"), qp, BadRequest)
  }

  they should "pass enumerated optional string query parameters iff the request provides a valid enum value" in {
    val oqp = QueryParameter[Option[String]]('qp, None, None, Some(Seq(Some("value1"), Some("value2"), Some("value3"))))
    converterTest[Option[String], QueryParameter[Option[String]]](Get("http://localhost?qp=value1"), oqp, OK)
    converterTest[Option[String], QueryParameter[Option[String]]](Get("http://localhost?qp=NoNoNo"), oqp, BadRequest)
  }

  they should "pass enumerated boolean query parameters iff the request provides a valid enum value" in {
    val qp = QueryParameter[Boolean]('qp, None, None, Some(Seq(false)))
    converterTest[Boolean, QueryParameter[Boolean]](Get("http://localhost?qp=false"), qp, OK)
    converterTest[Boolean, QueryParameter[Boolean]](Get("http://localhost?qp=true"), qp, BadRequest)
  }

  they should "pass enumerated optional boolean query parameters iff the request provides a valid enum value" in {
    val qp = QueryParameter[Option[Boolean]]('qp, None, None, Some(Seq(Some(false))))
    converterTest[Option[Boolean], QueryParameter[Option[Boolean]]](Get("http://localhost?qp=false"), qp, OK)
    converterTest[Option[Boolean], QueryParameter[Option[Boolean]]](Get("http://localhost?qp=true"), qp, BadRequest)
  }

  they should "pass enumerated int query parameters iff the request provides a valid enum value" in {
    val qp = QueryParameter[Int]('qp, None, None, Some(Seq(1, 2, 3)))
    converterTest[Int, QueryParameter[Int]](Get("http://localhost?qp=2"), qp, OK)
    converterTest[Int, QueryParameter[Int]](Get("http://localhost?qp=4"), qp, BadRequest)
  }

  they should "pass enumerated optional int query parameters iff the request provides a valid enum value" in {
    val qp = QueryParameter[Option[Int]]('qp, None, None, Some(Seq(Some(1), Some(2), Some(3))))
    converterTest[Option[Int], QueryParameter[Option[Int]]](Get("http://localhost?qp=2"), qp, OK)
    converterTest[Option[Int], QueryParameter[Option[Int]]](Get("http://localhost?qp=4"), qp, BadRequest)
  }

  they should "pass enumerated long query parameters iff the request provides a valid enum value" in {
    val qp = QueryParameter[Long]('qp, enum = Some(Seq(1, 2, 3)))
    converterTest[Long, QueryParameter[Long]](Get("http://localhost?qp=2"), qp, OK)
    converterTest[Long, QueryParameter[Long]](Get("http://localhost?qp=4"), qp, BadRequest)
  }

  they should "pass enumerated optional long query parameters iff the request provides a valid enum value" in {
    val qp = QueryParameter[Option[Long]]('qp, enum = Some(Seq(Some(1), Some(2), Some(3))))
    converterTest[Option[Long], QueryParameter[Option[Long]]](Get("http://localhost?qp=2"), qp, OK)
    converterTest[Option[Long], QueryParameter[Option[Long]]](Get("http://localhost?qp=4"), qp, BadRequest)
  }

  they should "pass enumerated float query parameters iff the request provides a valid enum value" in {
    val qp = QueryParameter[Float]('qp, enum = Some(Seq(Float.MaxValue)))
    converterTest[Float, QueryParameter[Float]](Get(s"http://localhost?qp=${Float.MaxValue}"), qp, OK)
    converterTest[Float, QueryParameter[Float]](Get("http://localhost?qp=4"), qp, BadRequest)
  }

  they should "pass enumerated optional float query parameters iff the request provides a valid enum value" in {
    val qp = QueryParameter[Option[Float]]('qp, enum = Some(Seq(Some(Float.MaxValue))))
    converterTest[Option[Float], QueryParameter[Option[Float]]](Get(s"http://localhost?qp=${Float.MaxValue}"), qp, OK)
    converterTest[Option[Float], QueryParameter[Option[Float]]](Get("http://localhost?qp=4"), qp, BadRequest)
  }

  they should "pass enumerated double query parameters iff the request provides a valid enum value" in {
    val qp = QueryParameter[Double]('qp, enum = Some(Seq(Double.MaxValue)))
    converterTest[Double, QueryParameter[Double]](Get(s"http://localhost?qp=${Double.MaxValue}"), qp, OK)
    converterTest[Double, QueryParameter[Double]](Get("http://localhost?qp=4"), qp, BadRequest)
  }

  they should "pass enumerated optional double query parameters iff the request provides a valid enum value" in {
    val qp = QueryParameter[Option[Double]]('qp, enum = Some(Seq(Some(Double.MaxValue))))
    converterTest[Option[Double], QueryParameter[Option[Double]]](Get(s"http://localhost?qp=${Double.MaxValue}"), qp, OK)
    converterTest[Option[Double], QueryParameter[Option[Double]]](Get("http://localhost?qp=4"), qp, BadRequest)
  }
}
