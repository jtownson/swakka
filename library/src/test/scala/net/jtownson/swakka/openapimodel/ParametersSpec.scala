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

package net.jtownson.swakka.openapimodel

import net.jtownson.swakka.openapimodel.BodyParameter.OpenBodyParameter
import net.jtownson.swakka.openapimodel.FormFieldParameter.OpenFormFieldParameter
import net.jtownson.swakka.openapimodel.HeaderParameter.OpenHeaderParameter
import net.jtownson.swakka.openapimodel.PathParameter.OpenPathParameter
import net.jtownson.swakka.openapimodel.QueryParameter.OpenQueryParameter
import net.jtownson.swakka.openapimodel.{QueryParameter, _}
import org.scalatest.FlatSpec
import org.scalatest.Matchers._
import org.scalatest.prop.TableDrivenPropertyChecks._

class ParametersSpec extends FlatSpec {

  val defaultValue = "a default value"

  val openParams = Table[String, Parameter[String]](
    ("test case", "parameter"),
    ("qp", OpenQueryParameter[String]('qp, None, None, None)),
    ("pp", OpenPathParameter[String]('pp, None, None, None)),
    ("hp", OpenHeaderParameter[String]('hp, None, None, None)),
    ("bp", OpenBodyParameter[String]('bp, None, None, None))
  )

  "An OpenParameter" should "throw when trying to obtain its value" in {
    forAll(openParams) { (_, parameter) =>
      assertThrows[IllegalStateException] {
        parameter.value
      }
    }
  }

  "An OpenParameter, when closed with a value" should "should provide this value" in {
    forAll(openParams) { (_, parameter) =>
      val value = "a value"
      parameter.asInstanceOf[OpenParameter[String, ClosedParameter[String, _]]].closeWith(value).value shouldBe value
    }
  }

  "A query parameter" should "provide values to a pattern match" in {
    val expectedValue = "foo"

    val param = OpenQueryParameter[String]('p, None, None, None).closeWith(expectedValue)

    param match {
      case QueryParameter(actualValue) => actualValue shouldBe expectedValue
      case _ => fail("Pattern does not match")
    }
  }

  "A path parameter" should "provide values to a pattern match" in {
    val expectedValue = "foo"

    val param = OpenPathParameter[String]('p, None, None, None).closeWith(expectedValue)

    param match {
      case PathParameter(actualValue) => actualValue shouldBe expectedValue
      case _ => fail("Pattern does not match")
    }
  }

  "A header parameter" should "provide values to a pattern match" in {
    val expectedValue = "foo"

    val param = OpenHeaderParameter[String]('p, None, None, None).closeWith(expectedValue)

    param match {
      case HeaderParameter(actualValue) => actualValue shouldBe expectedValue
      case _ => fail("Pattern does not match")
    }
  }

  "A body parameter" should "provide values to a pattern match" in {
    val expectedValue = "foo"

    val param = OpenBodyParameter[String]('p, None, None, None).closeWith(expectedValue)

    param match {
      case BodyParameter(actualValue) => actualValue shouldBe expectedValue
      case _ => fail("Pattern does not match")
    }
  }

  "A form field parameter" should "provide values to a pattern match" in {

    val expectedValue = "foo"

    val param = OpenFormFieldParameter('p, None, None, None).closeWith(expectedValue)

    param match {
      case FormFieldParameter(actualValue) => actualValue shouldBe expectedValue
      case _ => fail("Pattern does not match")
    }
  }
}
