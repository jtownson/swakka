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

import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.{Directive1, Route}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes.{BadRequest, OK}
import net.jtownson.swakka.coreroutegen.ConvertibleToDirective.{Aux, converter}
import spray.json._
import net.jtownson.swakka.openapijson._
import net.jtownson.swakka.openapimodel._
import net.jtownson.swakka.coreroutegen._
import org.scalatest.FlatSpec

class BodyParamConvertersSpec extends FlatSpec with ConverterTest {

  implicit val petFormat = jsonFormat2(Pet)

  "BodyParamConverters" should "convert a body param of String" in {
    converterTest[String, BodyParameter[String]](post("/p", "Hello"), BodyParameter[String]('p), OK, extractionAssertion("Hello"))
    converterTest[Option[String], BodyParameter[Option[String]]](post("/p", "Hello"), BodyParameter[Option[String]]('p), OK, extractionAssertion(Option("Hello")))
    converterTest[Option[String], BodyParameter[Option[String]]](post("/p"), BodyParameter[Option[String]]('p), OK, extractionAssertion(Option.empty[String]))
    converterTest[String, BodyParameter[String]](post("/p"), BodyParameter[String]('p, default = Some("Hello")), OK, extractionAssertion("Hello"))
    converterTest[Option[String], BodyParameter[Option[String]]](post("/p"), BodyParameter[Option[String]]('p, default = Some(Some("Hello"))), OK, extractionAssertion(Option("Hello")))
  }

  they should "convert a required body param of a case class" in {

    val pet = Pet(1, "tiddles")
    converterTest[Pet, BodyParameter[Pet]](post("/p", pet.toJson.compactPrint), BodyParameter[Pet]('p), OK, extractionAssertion(pet))
  }

  they should "convert an optional body param of a case class" in {

    val c: Aux[BodyParameter[Option[Pet]], Option[Pet]] = converter[BodyParameter[Option[Pet]], Option[Pet]]

    val route: Route = c.convertToDirective("", BodyParameter[Option[Pet]]('p)) { (maybePet: Option[Pet]) =>
      maybePet.map(_ => fail("should have got nothing")).getOrElse(complete("got nothing"))
    }

    converterTest[Option[Pet], BodyParameter[Option[Pet]]](post("/p"), route, "got nothing")
  }
}
