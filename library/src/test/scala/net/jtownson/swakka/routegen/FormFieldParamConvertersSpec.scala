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

import akka.http.scaladsl.model.StatusCodes.{BadRequest, OK}
import akka.http.scaladsl.model._
import akka.http.scaladsl.marshalling.Marshaller._
import akka.http.scaladsl.server.directives.FileInfo
import akka.stream.scaladsl.Source
import akka.util.ByteString
import net.jtownson.swakka.model.Parameters.FormFieldParameter
import net.jtownson.swakka.routegen.ConvertibleToDirective._
import org.scalatest.{Assertion, FlatSpec}
import org.scalatest.Matchers._


class FormFieldParamConvertersSpec extends FlatSpec with ConverterTest {

  private def form(key: String, value: String): HttpRequest =
    Post("/", FormData(key -> value))

  val multipartForm: HttpRequest = {
    val multipartForm =
      Multipart.FormData(Multipart.FormData.BodyPart.Strict(
        "fp",
        HttpEntity(ContentTypes.`text/plain(UTF-8)`, "2,3,5\n7,11,13,17,23\n29,31,37\n"),
        Map("filename" -> "primes.csv")))
    Post("/", multipartForm)
  }

  val missingFieldForm: HttpRequest = {
    val multipartForm =
      Multipart.FormData(Multipart.FormData.BodyPart.Strict(
        "other",
        HttpEntity(ContentTypes.`text/plain(UTF-8)`, "values"),
        Map()))
    Post("/", multipartForm)
  }

  private val fileInfoAssertion: ((FileInfo, Source[ByteString, Any])) => Assertion =
    _._1 shouldBe FileInfo("fp", "primes.csv", ContentTypes.`text/plain(UTF-8)`)

  private val optionalFileInfoAssertion: (Option[(FileInfo, Source[ByteString, Any])]) => Assertion =
    _.get._1 shouldBe FileInfo("fp", "primes.csv", ContentTypes.`text/plain(UTF-8)`)

  private def extractionAssertion[T](t: T): T => Assertion =
    _ shouldBe t

  "FormFieldParamConverters" should "accept and marshal mandatory params with data posted (and not provide default)" in {
    converterTest(form("fp", "v"), FormFieldParameter[String]('fp, None, Some("default")), OK, extractionAssertion("v"))
    converterTest(form("fp", "true"), FormFieldParameter[Boolean]('fp, None, Some(false)), OK, extractionAssertion(true))
    converterTest(form("fp", "1"), FormFieldParameter[Int]('fp, None, Some(2)), OK, extractionAssertion(1))
    converterTest(form("fp", "1"), FormFieldParameter[Long]('fp, None, Some(2)), OK, extractionAssertion(1L))
    converterTest(form("fp", "0"), FormFieldParameter[Float]('fp, None, Some(2)), OK, extractionAssertion(0.0F))
    converterTest(form("fp", "0"), FormFieldParameter[Double]('fp, None, Some(2)), OK, extractionAssertion(0.0D))
    converterTest(multipartForm, FormFieldParameter[(FileInfo, Source[ByteString, Any])]('fp, None, None), OK, fileInfoAssertion)
  }

  // should accept and marshal an optional param with data posted (and not provide default)
  they should "accept and marshal optional params with data posted (and not provide default)" in {
    converterTest(form("fp", "v"), FormFieldParameter[Option[String]]('fp, None, Some(Some("default"))), OK, extractionAssertion(Option("v")))
    converterTest(form("fp", "true"), FormFieldParameter[Option[Boolean]]('fp, None, Some(Some(false))), OK, extractionAssertion(Option(true)))
    converterTest(form("fp", "1"), FormFieldParameter[Option[Int]]('fp, None, Some(Some(2))), OK, extractionAssertion(Option(1)))
    converterTest(form("fp", "1"), FormFieldParameter[Option[Long]]('fp, None, Some(Some(2))), OK, extractionAssertion(Option(1L)))
    converterTest(form("fp", "0"), FormFieldParameter[Option[Float]]('fp, None, Some(Some(2))), OK, extractionAssertion(Option(0.0F)))
    converterTest(form("fp", "0"), FormFieldParameter[Option[Double]]('fp, None, Some(Some(2))), OK, extractionAssertion(Option(0.0D)))
    converterTest(multipartForm, FormFieldParameter[Option[(FileInfo, Source[ByteString, Any])]]('fp, None, None), OK, optionalFileInfoAssertion)
  }

  // should accept an optional param without default and provide none
  they should "accept optional params without defaults or data posted and provide None)" in {
    converterTest(form("other", "v"), FormFieldParameter[Option[String]]('fp), OK, extractionAssertion(Option.empty[String]))
    converterTest(form("other", "true"), FormFieldParameter[Option[Boolean]]('fp), OK, extractionAssertion(Option.empty[Boolean]))
    converterTest(form("other", "1"), FormFieldParameter[Option[Int]]('fp), OK, extractionAssertion(Option.empty[Int]))
    converterTest(form("other", "1"), FormFieldParameter[Option[Long]]('fp), OK, extractionAssertion(Option.empty[Long]))
    converterTest(form("other", "0"), FormFieldParameter[Option[Float]]('fp), OK, extractionAssertion(Option.empty[Float]))
    converterTest(form("other", "0"), FormFieldParameter[Option[Double]]('fp), OK, extractionAssertion(Option.empty[Double]))
    converterTest(missingFieldForm, FormFieldParameter[Option[(FileInfo, Source[ByteString, Any])]]('fp), OK, extractionAssertion(Option.empty[(FileInfo, Source[ByteString, Any])]))
  }


  // should reject a mandatory param without default
  they should "reject mandatory params without defaults or data posted" in {
    converterTest[String, FormFieldParameter[String]](form("other", "v"), FormFieldParameter[String]('fp), BadRequest)
    converterTest[Boolean, FormFieldParameter[Boolean]](form("other", "true"), FormFieldParameter[Boolean]('fp), BadRequest)
    converterTest[Int, FormFieldParameter[Int]](form("other", "1"), FormFieldParameter[Int]('fp), BadRequest)
    converterTest[Long, FormFieldParameter[Long]](form("other", "1"), FormFieldParameter[Long]('fp), BadRequest)
    converterTest[Float, FormFieldParameter[Float]](form("other", "0"), FormFieldParameter[Float]('fp), BadRequest)
    converterTest[Double, FormFieldParameter[Double]](form("other", "0"), FormFieldParameter[Double]('fp), BadRequest)
    converterTest[(FileInfo, Source[ByteString, Any]), FormFieldParameter[(FileInfo, Source[ByteString, Any])]](missingFieldForm, FormFieldParameter[(FileInfo, Source[ByteString, Any])]('fp, None, None), BadRequest)
  }


  // should accept an optional param with default and provide default when no data posted
  // should accept a mandatory param with default and provide default when no data posted

  // should accept an optional param with enum if data posted is in enum
  // should accept a mandatory param with enum if data posted is in enum

  // should reject an optional param with enum if data posted is not in enum
  // should reject a mandatory param with enum if data posted is not in enum

  // should reject a default that is not in enum when providing default for mandatory param
  // should reject a default that is not in enum when providing default for optional param
}
