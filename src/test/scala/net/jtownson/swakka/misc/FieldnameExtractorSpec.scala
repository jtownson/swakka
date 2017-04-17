package net.jtownson.swakka.misc

import net.jtownson.swakka.misc.FieldnameExtractor.fieldNames
import org.scalatest.Matchers._
import org.scalatest._

class FieldnameExtractorSpec extends FlatSpec {

  case class A(id: Int, value: String)
  case class B(id: Int, a: A)
  case class X()

  "FieldnameExtractor" should "get the fieldnames of a case class" in {
    fieldNames[A] shouldBe List("id", "value")
    fieldNames[B] shouldBe List("id", "a")
    fieldNames[X] shouldBe Nil
  }

}
