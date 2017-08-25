package net.jtownson.swakka.model

import net.jtownson.swakka.model.Parameters.BodyParameter.OpenBodyParameter
import net.jtownson.swakka.model.Parameters.FormParameter.OpenFormParameter
import net.jtownson.swakka.model.Parameters.HeaderParameter.OpenHeaderParameter
import net.jtownson.swakka.model.Parameters.PathParameter.OpenPathParameter
import net.jtownson.swakka.model.Parameters.QueryParameter.OpenQueryParameter
import net.jtownson.swakka.model.Parameters.{QueryParameter, _}
import org.scalatest.FlatSpec
import org.scalatest.Matchers._
import org.scalatest.prop.TableDrivenPropertyChecks._

class ParametersSpec extends FlatSpec {

  val defaultValue = "a default value"

  val openParams = Table[String, Parameter[String]](
    ("test case", "parameter"),
    ("qp", OpenQueryParameter[String]('qp, None, None)),
    ("pp", OpenPathParameter[String]('pp, None, None)),
    ("hp", OpenHeaderParameter[String]('hp, None, None)),
    ("bp", OpenBodyParameter[String]('bp, None, None))
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

    val param = OpenQueryParameter[String]('p, None, None).closeWith(expectedValue)

    param match {
      case QueryParameter(actualValue) => actualValue shouldBe expectedValue
      case _ => fail("Pattern does not match")
    }
  }

  "A path parameter" should "provide values to a pattern match" in {
    val expectedValue = "foo"

    val param = OpenPathParameter[String]('p, None, None).closeWith(expectedValue)

    param match {
      case PathParameter(actualValue) => actualValue shouldBe expectedValue
      case _ => fail("Pattern does not match")
    }
  }

  "A header parameter" should "provide values to a pattern match" in {
    val expectedValue = "foo"

    val param = OpenHeaderParameter[String]('p, None, None).closeWith(expectedValue)

    param match {
      case HeaderParameter(actualValue) => actualValue shouldBe expectedValue
      case _ => fail("Pattern does not match")
    }
  }

  "A body parameter" should "provide values to a pattern match" in {
    val expectedValue = "foo"

    val param = OpenBodyParameter[String]('p, None, None).closeWith(expectedValue)

    param match {
      case BodyParameter(actualValue) => actualValue shouldBe expectedValue
      case _ => fail("Pattern does not match")
    }
  }

  case class A(field: String)

  "A form parameter" should "provide values to a pattern match" in {

    val expectedValue = A("foo")

    val param = OpenFormParameter('p, None, None, A).closeWith(expectedValue)

    param match {
      case FormParameter(actualValue) => actualValue shouldBe expectedValue
      case _ => fail("Pattern does not match")
    }
  }
}
