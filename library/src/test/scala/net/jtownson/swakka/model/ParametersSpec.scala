package net.jtownson.swakka.model

import net.jtownson.swakka.model.Parameters._
import org.scalatest.FlatSpec
import org.scalatest.Matchers._
import org.scalatest.prop.TableDrivenPropertyChecks._

class ParametersSpec extends FlatSpec {

  val defaultValue = "a default value"

  val openParams = Table[String, Parameter[String]](
    ("test case", "parameter"),
    ("qp", QueryParameter[String]('qp, None, false)),
    ("pp", PathParameter[String]('pp, None, false)),
    ("hp", HeaderParameter[String]('hp, None, false)),
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

  "An OpenParameter, when closed without a value" should "reject if no default is available" in {

    forAll(openParams) { (_, parameter) =>

    }
  }

  val paramsWithDefault = Table[String, Parameter[String]](
    ("test case", "parameter"),
    ("qp", QueryParameter[String]('qp, None, false, Some(defaultValue))),
    ("pp", PathParameter[String]('pp, None, false, Some(defaultValue))),
    ("hp", HeaderParameter[String]('hp, None, false, Some(defaultValue))),
    ("bp", BodyParameter[String]('bp, None, false, Some(defaultValue)))
  )

  "An OpenParameter, when closed without a value" should "provide its default" in {

    forAll(paramsWithDefault) { (_, parameter) =>
//      parameter.asInstanceOf[OpenParameter[String, ClosedParameter[String]]]..closeWithDefault.value shouldBe defaultValue
    }
  }


}
