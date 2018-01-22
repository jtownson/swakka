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

import akka.http.scaladsl.model.{HttpHeader, HttpRequest}
import akka.http.scaladsl.model.StatusCodes.{BadRequest, OK}
import akka.http.scaladsl.model.headers.RawHeader
import net.jtownson.swakka.openapimodel._
import org.scalatest.FlatSpec
import org.scalatest.prop.TableDrivenPropertyChecks._

class MultiParamConvertersSpec extends FlatSpec with ConverterTest {

  "MultiParamConverters" should "convert multi query parameters" in {
    converterTest(
      Get(s"http://example.com?status=a1&status=a2"),
      MultiValued[String, QueryParameter[String]](QueryParameter[String]('status)),
      OK,
      extractionAssertion(Seq("a1", "a2")))
  }

  val stringQueryFormatCases = Table(
    ("description", "format", "request"),
    ("Multi param", multi, Get(s"http://example.com?status=a1&status=a2")),
    ("Pipe separated", pipes, Get(s"http://example.com?status=a1%7Ca2")),
    ("Comma separated", csv, Get(s"http://example.com?status=a1%2Ca2")),
    ("Tab separated", tsv, Get(s"http://example.com?status=a1%09a2")),
    ("Space separated", ssv, Get(s"http://example.com?status=a1%20a2"))
  )

  forAll(stringQueryFormatCases) { (description, format, request) =>
    "multi query params" should s"support '$description' collection format for strings" in {
      converterTest(
        request,
        MultiValued[String, QueryParameter[String]](QueryParameter[String]('status), format),
        OK,
        extractionAssertion(Seq("a1", "a2")))

      converterTest(
        request,
        MultiValued[String, QueryParameterConstrained[String, String]](QueryParameterConstrained[String, String](name = 'status, constraints = Constraints()), format),
        OK,
        extractionAssertion(Seq("a1", "a2")))
    }
  }

  val intQueryFormatCases = Table(
    ("description", "format", "request"),
    ("Multi param", multi, Get(s"http://example.com?status=1&status=2")),
    ("Pipe separated", pipes, Get(s"http://example.com?status=1%7C2")),
    ("Comma separated", csv, Get(s"http://example.com?status=1%2C2")),
    ("Tab separated", tsv, Get(s"http://example.com?status=1%092")),
    ("Space separated", ssv, Get(s"http://example.com?status=1%202"))
  )

  forAll(intQueryFormatCases) { (description, format, request) =>
    "multi query params" should s"support '$description' collection format for ints" in {
      converterTest(
        request,
        MultiValued[Int, QueryParameter[Int]](QueryParameter[Int]('status), format),
        OK,
        extractionAssertion(Seq(1, 2)))
    }
  }

  private def hGet(httpHeaders: HttpHeader*): HttpRequest =
    Get(s"http://example.com?status=1&status=2").withHeaders(httpHeaders: _*)

  val intHeaderFormatCases = Table(
    ("description", "format", "request"),
    ("Multi param", multi, hGet(RawHeader("x", "1"), RawHeader("x", "2"))),
    ("Pipe separated", pipes, hGet(RawHeader("x", "1|2"))),
    ("Comma separated", csv, hGet(RawHeader("x", "1,2"))),
    ("Tab separated", tsv, hGet(RawHeader("x", "1\t2"))),
    ("Space separated", ssv, hGet(RawHeader("x", "1 2")))
  )

  forAll(intHeaderFormatCases) { (description, format, request) =>
    "multi header params" should s"support '$description' collection format for ints" in {
      converterTest(
        request,
        MultiValued[Int, HeaderParameter[Int]](HeaderParameter[Int]('x), format),
        OK,
        extractionAssertion(Seq(1, 2)))
    }
  }

  they should "provide missing values as an empty Seq" in {
    converterTest(
      Get(s"http://example.com"),
      MultiValued[String, QueryParameter[String]](QueryParameter[String]('status)),
      OK,
      extractionAssertion(Seq[String]()))
  }

  they should "provide defaults for missing values (with default via inner parameter)" in {
    converterTest(
      Get(s"http://example.com"),
      MultiValued[String, QueryParameter[String]](QueryParameter[String](name = 'status, default = Some("a"))),
      OK,
      extractionAssertion(Seq("a")))
  }

  they should "provide defaults for missing values (with default via multi parameter)" in {

    val qp = QueryParameter[String]('status)

    converterTest(
      Get(s"http://example.com"),
      MultiValued[String, QueryParameter[String]](qp, multi, Some(Seq("a", "b"))),
      OK,
      extractionAssertion(Seq("a", "b")))
  }

  they should "validate provided values are in an enum" in {

    val qp = QueryParameterConstrained[String, String](name = 'status, constraints = Constraints(enum = Some(Set("a", "b"))))

    // values provided and within the enum
    converterTest[Seq[String], MultiValued[String, QueryParameterConstrained[String, String]]](
      Get(s"http://example.com?status=a&status=b"),
      MultiValued[String, QueryParameterConstrained[String, String]](qp),
      OK,
      extractionAssertion(Seq("a", "b")))

    // values provided but outside the enum
    converterTest[Seq[String], MultiValued[String, QueryParameterConstrained[String, String]]](
      Get(s"http://example.com?status=c"),
      MultiValued[String, QueryParameterConstrained[String, String]](qp),
      BadRequest)

    // values missing but inner default is within the enum
    converterTest[Seq[String], MultiValued[String, QueryParameterConstrained[String, String]]](
      Get(s"http://example.com"),
      MultiValued[String, QueryParameterConstrained[String, String]](QueryParameterConstrained[String, String](name = 'status, default = Some("a"), constraints = Constraints(enum = Some(Set("a", "b"))))),
      OK,
      extractionAssertion(Seq("a")))

    // values missing but inner default is outside the enum
    converterTest[Seq[String], MultiValued[String, QueryParameterConstrained[String, String]]](
      Get(s"http://example.com"),
      MultiValued[String, QueryParameterConstrained[String, String]](QueryParameterConstrained[String, String](name = 'status, default = Some("c"), constraints = Constraints(enum = Some(Set("a", "b"))))),
      BadRequest)
  }
}
