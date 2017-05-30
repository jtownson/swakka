package net.jtownson.swakka.model

import net.jtownson.swakka.model.Parameters._
import org.scalatest.FlatSpec
import org.scalatest.Matchers._
import org.scalatest.prop.TableDrivenPropertyChecks._

class ParametersSpec extends FlatSpec {

  val defaultValue = "a default value"

  val openParams = Table[String, Parameter[String]](
    ("test case", "parameter"),
    ("qp", QueryParameter[String]('qp, None)),
    ("pp", PathParameter[String]('pp, None)),
    ("hp", HeaderParameter[String]('hp, None)),
    ("bp", BodyParameter[String]('bp))
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
}
