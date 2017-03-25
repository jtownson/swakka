package net.jtownson.minimal

import net.jtownson.minimal.FieldnameExtractor.fieldNames
import org.scalatest._
import org.scalatest.Matchers._

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
