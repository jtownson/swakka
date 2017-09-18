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

import akka.http.scaladsl.model.StatusCodes.OK
import akka.http.scaladsl.model._
import akka.http.scaladsl.marshalling.Marshaller._
import akka.http.scaladsl.server.directives.FileInfo
import akka.stream.scaladsl.Source
import akka.util.ByteString
import net.jtownson.swakka.model.Parameters.FormFieldParameter
import net.jtownson.swakka.routegen.ConvertibleToDirective._
import org.scalatest.FlatSpec
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

  val multipartResponse: (FileInfo, Source[ByteString, Any]) = (
    FileInfo("filename", "primes.csv", ContentTypes.`text/plain(UTF-8)`),
    Source.fromFuture(Future(ByteString("2,3,5\n7,11,13,17,23\n29,31,37\n"))))

  // should accept and marshal a mandatory param with data posted (and not provide default)
  "FormFieldParamConverters" should "accept and marshal a mandatory param with data posted (and not provide default)" in {
    converterTest(form("k", "v"), FormFieldParameter[String]('fp, None, Some("default")), OK, "v", "")
    converterTest(form("k", "true"), FormFieldParameter[Boolean]('fp, None, Some(false)), OK, true, "")
    converterTest(form("k", "1"), FormFieldParameter[Int]('fp, None, Some(2)), OK, 1, "")
    converterTest(form("k", "1"), FormFieldParameter[Long]('fp, None, Some(2)), OK, 1L, "")
    converterTest(form("k", "0"), FormFieldParameter[Float]('fp, None, Some(2)), OK, 0.0F, "")
    converterTest(form("k", "0"), FormFieldParameter[Double]('fp, None, Some(2)), OK, 0.0D, "")
    converterTest(form("k", "0"), FormFieldParameter[Double]('fp, None, Some(2)), OK, 0.0D, "")
    converterTest(multipartForm, FormFieldParameter[(FileInfo, Source[ByteString, Any])]('fp, None, None), OK, multipartResponse, "")
  }

  // should accept and marshal an optional param with data posted (and not provide default)

  // should accept an optional param without default and provide none
  // should reject a mandatory param without default

  // should accept an optional param with default and provide default when no data posted
  // should accept a mandatory param with default and provide default when no data posted

  // should accept an optional param with enum if data posted is in enum
  // should accept a mandatory param with enum if data posted is in enum

  // should reject an optional param with enum if data posted is not in enum
  // should reject a mandatory param with enum if data posted is not in enum

  // should reject a default that is not in enum when providing default for mandatory param
  // should reject a default that is not in enum when providing default for optional param
}
