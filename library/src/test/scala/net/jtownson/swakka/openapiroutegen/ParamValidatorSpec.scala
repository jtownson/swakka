package net.jtownson.swakka.openapiroutegen

import net.jtownson.swakka.openapimodel.Constraints
import net.jtownson.swakka.openapiroutegen.ParamValidator._

import org.scalatest.FlatSpec
import org.scalatest.Matchers._
import org.scalatest.prop.TableDrivenPropertyChecks._

class ParamValidatorSpec extends FlatSpec {

  val anyCases = Table(
    ("name", "value", "constraints", "validator", "expected"),
    ("valid enum", true, Constraints[Boolean](enum = Some(Set(true))), anyValidator[Boolean], Right(true)),
    ("invalid enum", true, Constraints[Boolean](enum = Some(Set(false))), anyValidator[Boolean], Left("""The value "true" is not in the specified enum [false]"""))
  )

  val stringCases = Table(
    ("name", "value", "constraints", "validator", "expected"),
    ("valid enum", "X", Constraints[String](enum = Some(Set("X"))), stringValidator, Right("X")),
    ("invalid enum", "X", Constraints[String](enum = Some(Set("Y"))), stringValidator, Left("""The value "X" is not in the specified enum [Y]""")),
    ("valid maxLength", "ABC", Constraints[String](maxLength = Some(3)), stringValidator, Right("ABC")),
    ("invalid maxLength", "ABCD", Constraints[String](maxLength = Some(3)), stringValidator, Left("""The value "ABCD" is longer than the maxLength of 3""")),
    ("valid minLength", "ABC", Constraints[String](minLength = Some(3)), stringValidator, Right("ABC")),
    ("invalid minLength", "AB", Constraints[String](minLength = Some(3)), stringValidator, Left("""The value "AB" is shorter than the minLength of 3""")),
    ("valid pattern", "ABC", Constraints[String](pattern = Some("^ABC$")), stringValidator, Right("ABC")),
    ("invalid pattern", "ABC", Constraints[String](pattern = Some("^ABCD$")), stringValidator, Left("""The value "ABC" does not match pattern "^ABCD$""""))
  )

  val numericCases = Table(
    ("name", "value", "constraints", "validator", "expected"),

    ("valid enum", 10, Constraints[Int](enum = Some(Set(10))), numberValidator[Int], Right(10)),
    ("invalid enum", 10, Constraints[Int](enum = Some(Set(1))), numberValidator[Int], Left("""The value "10" is not in the specified enum [1]""")),

    ("valid multipleOf", 10, Constraints[Int](multipleOf = Some(2)), integralValidator[Int], Right(10)),
    ("invalid multipleOf", 10, Constraints[Int](multipleOf = Some(3)), integralValidator[Int], Left("""The value 10 is not a multiple of 3""")),

    ("valid maximum", 10, Constraints[Int](maximum = Some(10)), numberValidator[Int], Right(10)),
    ("invalid maximum", 10, Constraints[Int](maximum = Some(9)), numberValidator[Int], Left("""The value 10 is larger than the maximum of 9""")),

    ("valid minimum", 10, Constraints[Int](minimum = Some(10)), numberValidator[Int], Right(10)),
    ("invalid minimum", 10, Constraints[Int](minimum = Some(11)), numberValidator[Int], Left("""The value 10 is smaller than the minimum of 11""")),

    ("valid ex maximum", 10, Constraints[Int](exclusiveMaximum = Some(11)), numberValidator[Int], Right(10)),
    ("invalid ex maximum", 10, Constraints[Int](exclusiveMaximum = Some(10)), numberValidator[Int], Left("""The value 10 is not smaller than the exclusive maximum of 10""")),

    ("valid ex minimum", 10, Constraints[Int](exclusiveMinimum = Some(9)), numberValidator[Int], Right(10)),
    ("invalid ex minimum", 10, Constraints[Int](exclusiveMinimum = Some(10)), numberValidator[Int], Left("""The value 10 is not larger than the exclusive minimum of 10"""))
  )

  val optionCases = Table(
    ("name", "value", "constraints", "validator", "expected"),
    ("valid enum", Some(true), Constraints[Boolean](enum = Some(Set(true))), optionValidator(anyValidator[Boolean]), Right(Some(true))),
    ("invalid enum", Some(true), Constraints[Boolean](enum = Some(Set(false))), optionValidator(anyValidator[Boolean]), Left("""The value "true" is not in the specified enum [false]"""))
  )

  val sequenceCases = Table(
    ("name", "value", "constraints", "validator", "expected"),
    ("valid enum", Seq("A", "B"), Constraints[String](enum = Some(Set("A", "B", "C"))), sequenceValidator(stringValidator), Right(Seq("A", "B"))),
    ("invalid enum", Seq("B", "C"), Constraints[String](enum = Some(Set("A", "B"))), sequenceValidator(stringValidator), Left("""The value "C" is not in the specified enum [A, B]"""))
  )


  forAll(anyCases) { (name, value, constraints, validator, expected) =>
    s"AnyValidator $name" should "validate correctly" in {
      validator.validate(constraints, value) shouldBe expected
    }
  }

  forAll(stringCases) { (name, value, constraints, validator, expected) =>
    s"StringValidator $name" should "validate correctly" in {
      validator.validate(constraints, value) shouldBe expected
    }
  }

  forAll(numericCases) { (name, value, constraints, validator, expected) =>
    s"NumberValidator $name" should "validate correctly" in {
      validator.validate(constraints, value) shouldBe expected
    }
  }

  forAll(optionCases) { (name, value, constraints, validator, expected) =>
    s"OptionValidator $name" should "validate correctly" in {
      validator.validate(constraints, value) shouldBe expected
    }
  }

  forAll(sequenceCases) { (name, value, constraints, validator, expected) =>
    s"SequenceValidator $name" should "validate correctly" in {
      validator.validate(constraints, value) shouldBe expected
    }
  }
}
