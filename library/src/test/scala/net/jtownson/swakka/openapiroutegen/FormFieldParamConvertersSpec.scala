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

import akka.http.scaladsl.model.StatusCodes.{BadRequest, OK}
import akka.http.scaladsl.model._
import akka.http.scaladsl.marshalling.Marshaller._
import akka.http.scaladsl.server.directives.FileInfo
import akka.stream.scaladsl.Source
import akka.util.ByteString
import net.jtownson.swakka.openapimodel._
import org.scalatest.{Assertion, FlatSpec}
import org.scalatest.Matchers._

import scala.concurrent.Future


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

  val multipartResponse: (FileInfo, Source[ByteString, Any]) = (
    FileInfo("fp", "primes.csv", ContentTypes.`text/plain(UTF-8)`),
    Source.fromFuture(Future(ByteString("2,3,5\n7,11,13,17,23\n29,31,37\n"))))

  private val fileInfoAssertion: ((FileInfo, Source[ByteString, Any])) => Assertion =
    _._1 shouldBe FileInfo("fp", "primes.csv", ContentTypes.`text/plain(UTF-8)`)

  private val optionalFileInfoAssertion: (Option[(FileInfo, Source[ByteString, Any])]) => Assertion =
    _.get._1 shouldBe FileInfo("fp", "primes.csv", ContentTypes.`text/plain(UTF-8)`)


  "FormFieldParamConverters" should "accept and marshal mandatory params with data posted (and not accidentally marshal default)" in {
    converterTest(form("fp", "v"), FormFieldParameter[String]('fp, None, Some("default")), OK, extractionAssertion("v"))
    converterTest(form("fp", "true"), FormFieldParameter[Boolean]('fp, None, Some(false)), OK, extractionAssertion(true))
    converterTest(form("fp", "1"), FormFieldParameter[Int]('fp, None, Some(2)), OK, extractionAssertion(1))
    converterTest(form("fp", "1"), FormFieldParameter[Long]('fp, None, Some(2)), OK, extractionAssertion(1L))
    converterTest(form("fp", "0"), FormFieldParameter[Float]('fp, None, Some(2)), OK, extractionAssertion(0.0F))
    converterTest(form("fp", "0"), FormFieldParameter[Double]('fp, None, Some(2)), OK, extractionAssertion(0.0D))
    converterTest(multipartForm, FormFieldParameter[(FileInfo, Source[ByteString, Any])]('fp, None, None), OK, fileInfoAssertion)
  }

  they should "accept and marshal optional params with data posted (and not provide default)" in {
    converterTest(form("fp", "v"), FormFieldParameter[Option[String]]('fp, None, Some(Some("default"))), OK, extractionAssertion(Option("v")))
    converterTest(form("fp", "true"), FormFieldParameter[Option[Boolean]]('fp, None, Some(Some(false))), OK, extractionAssertion(Option(true)))
    converterTest(form("fp", "1"), FormFieldParameter[Option[Int]]('fp, None, Some(Some(2))), OK, extractionAssertion(Option(1)))
    converterTest(form("fp", "1"), FormFieldParameter[Option[Long]]('fp, None, Some(Some(2))), OK, extractionAssertion(Option(1L)))
    converterTest(form("fp", "0"), FormFieldParameter[Option[Float]]('fp, None, Some(Some(2))), OK, extractionAssertion(Option(0.0F)))
    converterTest(form("fp", "0"), FormFieldParameter[Option[Double]]('fp, None, Some(Some(2))), OK, extractionAssertion(Option(0.0D)))
    converterTest(multipartForm, FormFieldParameter[Option[(FileInfo, Source[ByteString, Any])]]('fp, None, None), OK, optionalFileInfoAssertion)
  }

  they should "accept optional params without defaults or data posted and provide None" in {
    converterTest(form("other", "v"), FormFieldParameter[Option[String]]('fp), OK, extractionAssertion(Option.empty[String]))
    converterTest(form("other", "true"), FormFieldParameter[Option[Boolean]]('fp), OK, extractionAssertion(Option.empty[Boolean]))
    converterTest(form("other", "1"), FormFieldParameter[Option[Int]]('fp), OK, extractionAssertion(Option.empty[Int]))
    converterTest(form("other", "1"), FormFieldParameter[Option[Long]]('fp), OK, extractionAssertion(Option.empty[Long]))
    converterTest(form("other", "0"), FormFieldParameter[Option[Float]]('fp), OK, extractionAssertion(Option.empty[Float]))
    converterTest(form("other", "0"), FormFieldParameter[Option[Double]]('fp), OK, extractionAssertion(Option.empty[Double]))
    converterTest(missingFieldForm, FormFieldParameter[Option[(FileInfo, Source[ByteString, Any])]]('fp), OK, extractionAssertion(Option.empty[(FileInfo, Source[ByteString, Any])]))
  }

  they should "reject mandatory params without defaults or data posted" in {
    converterTest[String, FormFieldParameter[String]](form("other", "v"), FormFieldParameter[String]('fp), BadRequest)
    converterTest[Boolean, FormFieldParameter[Boolean]](form("other", "true"), FormFieldParameter[Boolean]('fp), BadRequest)
    converterTest[Int, FormFieldParameter[Int]](form("other", "1"), FormFieldParameter[Int]('fp), BadRequest)
    converterTest[Long, FormFieldParameter[Long]](form("other", "1"), FormFieldParameter[Long]('fp), BadRequest)
    converterTest[Float, FormFieldParameter[Float]](form("other", "0"), FormFieldParameter[Float]('fp), BadRequest)
    converterTest[Double, FormFieldParameter[Double]](form("other", "0"), FormFieldParameter[Double]('fp), BadRequest)
    converterTest[(FileInfo, Source[ByteString, Any]), FormFieldParameter[(FileInfo, Source[ByteString, Any])]](missingFieldForm, FormFieldParameter[(FileInfo, Source[ByteString, Any])]('fp, None, None), BadRequest)
  }

  they should "accept an optional param with default and provide default when no data posted" in {
    converterTest(form("other", "v"), FormFieldParameter[Option[String]]('fp, None, Some(Some("default"))), OK, extractionAssertion(Option("default")))
    converterTest(form("other", "true"), FormFieldParameter[Option[Boolean]]('fp, None, Some(Some(false))), OK, extractionAssertion(Option(false)))
    converterTest(form("other", "1"), FormFieldParameter[Option[Int]]('fp, None, Some(Some(2))), OK, extractionAssertion(Option(2)))
    converterTest(form("other", "1"), FormFieldParameter[Option[Long]]('fp, None, Some(Some(2))), OK, extractionAssertion(Option(2L)))
    converterTest(form("other", "0"), FormFieldParameter[Option[Float]]('fp, None, Some(Some(1.0f))), OK, extractionAssertion(Option(1.0f)))
    converterTest(form("other", "0"), FormFieldParameter[Option[Double]]('fp, None, Some(Some(1.0))), OK, extractionAssertion(Option(1.0)))
    converterTest(missingFieldForm, FormFieldParameter[Option[(FileInfo, Source[ByteString, Any])]]('fp, None, Some(Some(multipartResponse))), OK, optionalFileInfoAssertion)
  }

  they should "accept a mandatory param with default and provide default when no data posted" in {
    converterTest(form("other", "v"), FormFieldParameter[String]('fp, None, Some("default")), OK, extractionAssertion("default"))
    converterTest(form("other", "true"), FormFieldParameter[Boolean]('fp, None, Some(false)), OK, extractionAssertion(false))
    converterTest(form("other", "1"), FormFieldParameter[Int]('fp, None, Some(2)), OK, extractionAssertion(2))
    converterTest(form("other", "1"), FormFieldParameter[Long]('fp, None, Some(2)), OK, extractionAssertion(2L))
    converterTest(form("other", "0"), FormFieldParameter[Float]('fp, None, Some(1.0f)), OK, extractionAssertion(1.0f))
    converterTest(form("other", "0"), FormFieldParameter[Double]('fp, None, Some(1.0)), OK, extractionAssertion(1.0))
    converterTest(missingFieldForm, FormFieldParameter[(FileInfo, Source[ByteString, Any])]('fp, None, Option(multipartResponse)), OK, fileInfoAssertion)
  }

  they should "accept an optional param with enum if data posted is in enum" in {
    converterTest(form("fp", "v"), FormFieldParameter[Option[String]]('fp, None, Some(Some("default")), Some(Seq(Some("v")))), OK, extractionAssertion(Option("v")))
    converterTest(form("fp", "true"), FormFieldParameter[Option[Boolean]]('fp, None, Some(Some(false)), Some(Seq(Some(true)))), OK, extractionAssertion(Option(true)))
    converterTest(form("fp", "1"), FormFieldParameter[Option[Int]]('fp, None, Some(Some(2)), Some(Seq(Some(1)))), OK, extractionAssertion(Option(1)))
    converterTest(form("fp", "1"), FormFieldParameter[Option[Long]]('fp, None, Some(Some(2)), Some(Seq(Some(1)))), OK, extractionAssertion(Option(1L)))
    converterTest(form("fp", "0"), FormFieldParameter[Option[Float]]('fp, None, Some(Some(2)), Some(Seq(Some(0.0f)))), OK, extractionAssertion(Option(0.0F)))
    converterTest(form("fp", "0"), FormFieldParameter[Option[Double]]('fp, None, Some(Some(2)), Some(Seq(Some(0.0)))), OK, extractionAssertion(Option(0.0D)))

    // TODO enum checking is problematic with non-strict entities.
    // TODO Consider doing file upload as strictly marshalled entities, similar to body parameters.
//    converterTest(multipartForm, FormFieldParameter[Option[(FileInfo, Source[ByteString, Any])]]('fp, None, None), OK, optionalFileInfoAssertion)
  }

  they should "accept a mandatory param with enum if data posted is in enum" in {
    converterTest(form("fp", "v"), FormFieldParameter[String]('fp, None, Some("default"), Some(Seq("v"))), OK, extractionAssertion("v"))
    converterTest(form("fp", "true"), FormFieldParameter[Boolean]('fp, None, Some(false), Some(Seq(true))), OK, extractionAssertion(true))
    converterTest(form("fp", "1"), FormFieldParameter[Int]('fp, None, Some(2), Some(Seq(1))), OK, extractionAssertion(1))
    converterTest(form("fp", "1"), FormFieldParameter[Long]('fp, None, Some(2), Some(Seq(1))), OK, extractionAssertion(1L))
    converterTest(form("fp", "0"), FormFieldParameter[Float]('fp, None, Some(2), Some(Seq(0.0f))), OK, extractionAssertion(0.0F))
    converterTest(form("fp", "0"), FormFieldParameter[Double]('fp, None, Some(2), Some(Seq(0.0))), OK, extractionAssertion(0.0D))
  }

  they should "reject an optional param with enum if data posted is in enum" in {
    converterTest[Option[String], FormFieldParameter[Option[String]]](form("fp", "v"), FormFieldParameter[Option[String]]('fp, None, Some(Some("default")), Some(Seq(Some("vv")))), BadRequest)
    converterTest[Option[Boolean], FormFieldParameter[Option[Boolean]]](form("fp", "true"), FormFieldParameter[Option[Boolean]]('fp, None, Some(Some(false)), Some(Seq(Some(false)))), BadRequest)
    converterTest[Option[Int], FormFieldParameter[Option[Int]]](form("fp", "1"), FormFieldParameter[Option[Int]]('fp, None, Some(Some(2)), Some(Seq(Some(2)))), BadRequest)
    converterTest[Option[Long], FormFieldParameter[Option[Long]]](form("fp", "1"), FormFieldParameter[Option[Long]]('fp, None, Some(Some(2)), Some(Seq(Some(2)))), BadRequest)
    converterTest[Option[Float], FormFieldParameter[Option[Float]]](form("fp", "0"), FormFieldParameter[Option[Float]]('fp, None, Some(Some(2)), Some(Seq(Some(1.0f)))), BadRequest)
    converterTest[Option[Double], FormFieldParameter[Option[Double]]](form("fp", "0"), FormFieldParameter[Option[Double]]('fp, None, Some(Some(2)), Some(Seq(Some(1.0)))), BadRequest)
  }

  they should "reject a mandatory param with enum if data posted is in enum" in {
    converterTest[String, FormFieldParameter[String]](form("fp", "v"), FormFieldParameter[String]('fp, None, Some("default"), Some(Seq("vv"))), BadRequest)
    converterTest[Boolean, FormFieldParameter[Boolean]](form("fp", "true"), FormFieldParameter[Boolean]('fp, None, Some(false), Some(Seq(false))), BadRequest)
    converterTest[Int, FormFieldParameter[Int]](form("fp", "1"), FormFieldParameter[Int]('fp, None, Some(2), Some(Seq(2))), BadRequest)
    converterTest[Long, FormFieldParameter[Long]](form("fp", "1"), FormFieldParameter[Long]('fp, None, Some(2), Some(Seq(2))), BadRequest)
    converterTest[Float, FormFieldParameter[Float]](form("fp", "0"), FormFieldParameter[Float]('fp, None, Some(2), Some(Seq(1.0f))), BadRequest)
    converterTest[Double, FormFieldParameter[Double]](form("fp", "0"), FormFieldParameter[Double]('fp, None, Some(2), Some(Seq(1.0))), BadRequest)
  }

  they should "reject a default that is not in enum when providing default for mandatory param" in {
    converterTest[String, FormFieldParameter[String]](form("other", "v"), FormFieldParameter[String]('fp, None, Some("default"), Some(Seq("vv"))), BadRequest)
    converterTest[Boolean, FormFieldParameter[Boolean]](form("other", "true"), FormFieldParameter[Boolean]('fp, None, Some(true), Some(Seq(false))), BadRequest)
    converterTest[Int, FormFieldParameter[Int]](form("other", "1"), FormFieldParameter[Int]('fp, None, Some(2), Some(Seq(1))), BadRequest)
    converterTest[Long, FormFieldParameter[Long]](form("other", "1"), FormFieldParameter[Long]('fp, None, Some(2), Some(Seq(1))), BadRequest)
    converterTest[Float, FormFieldParameter[Float]](form("other", "0"), FormFieldParameter[Float]('fp, None, Some(2), Some(Seq(1.0f))), BadRequest)
    converterTest[Double, FormFieldParameter[Double]](form("other", "0"), FormFieldParameter[Double]('fp, None, Some(2), Some(Seq(1.0))), BadRequest)
  }
  they should "reject a default that is not in enum when providing default for optional param" in {
    converterTest[Option[String], FormFieldParameter[Option[String]]](form("other", "v"), FormFieldParameter[Option[String]]('fp, None, Some(Some("default")), Some(Seq(Some("vv")))), BadRequest)
    converterTest[Option[Boolean], FormFieldParameter[Option[Boolean]]](form("other", "true"), FormFieldParameter[Option[Boolean]]('fp, None, Some(Some(true)), Some(Seq(Some(false)))), BadRequest)
    converterTest[Option[Int], FormFieldParameter[Option[Int]]](form("other", "1"), FormFieldParameter[Option[Int]]('fp, None, Some(Some(2)), Some(Seq(Some(1)))), BadRequest)
    converterTest[Option[Long], FormFieldParameter[Option[Long]]](form("other", "1"), FormFieldParameter[Option[Long]]('fp, None, Some(Some(2)), Some(Seq(Some(1)))), BadRequest)
    converterTest[Option[Float], FormFieldParameter[Option[Float]]](form("other", "0"), FormFieldParameter[Option[Float]]('fp, None, Some(Some(2)), Some(Seq(Some(1.0f)))), BadRequest)
    converterTest[Option[Double], FormFieldParameter[Option[Double]]](form("other", "0"), FormFieldParameter[Option[Double]]('fp, None, Some(Some(2)), Some(Seq(Some(1.0)))), BadRequest)
  }
}
