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
}
