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

package net.jtownson.swakka.routegen

import akka.http.scaladsl.model.StatusCodes.{OK, BadRequest}
import net.jtownson.swakka.model.Parameters.{MultiValued, QueryParameter}
import org.scalatest.FlatSpec

class MultiParamConvertersTest extends FlatSpec with ConverterTest {

  "MultiParamConverters" should "convert multi query parameters" in {
    converterTest(
      Get(s"http://example.com?status=a1&status=a2"),
      MultiValued[String, QueryParameter[String]](QueryParameter[String]('status)),
      OK,
      extractionAssertion(Seq("a1", "a2")))

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
      MultiValued[String, QueryParameter[String]](qp, Some(Seq("a", "b"))),
      OK,
      extractionAssertion(Seq("a", "b")))
  }

  they should "validate provided values are in an enum" in {

    val qp = QueryParameter[String](name = 'status, enum = Some(Seq("a", "b")))

    // values provided and within the enum
    converterTest[Seq[String], MultiValued[String, QueryParameter[String]]](
      Get(s"http://example.com?status=a&status=b"),
      MultiValued[String, QueryParameter[String]](qp),
      OK,
      extractionAssertion(Seq("a", "b")))

    // values provided but outside the enum
    converterTest[Seq[String], MultiValued[String, QueryParameter[String]]](
      Get(s"http://example.com?status=c"),
      MultiValued[String, QueryParameter[String]](qp),
      BadRequest)

    // values missing but inner default is within the enum
    converterTest[Seq[String], MultiValued[String, QueryParameter[String]]](
      Get(s"http://example.com"),
      MultiValued[String, QueryParameter[String]](QueryParameter[String](name = 'status, default = Some("a"), enum = Some(Seq("a", "b")))),
      OK,
      extractionAssertion(Seq("a")))

    // TODO For these cases it would be better to fail during param.apply
    // values missing but inner default is outside the enum
    converterTest[Seq[String], MultiValued[String, QueryParameter[String]]](
      Get(s"http://example.com"),
      MultiValued[String, QueryParameter[String]](QueryParameter[String](name = 'status, default = Some("c"), enum = Some(Seq("a", "b")))),
      BadRequest)
  }
}
