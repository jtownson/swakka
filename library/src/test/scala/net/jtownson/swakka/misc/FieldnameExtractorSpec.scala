package net.jtownson.swakka.misc

import net.jtownson.swakka.misc.FieldnameExtractor.{fieldNameTypes, fieldNames}
import org.scalatest.Matchers._
import org.scalatest._

class FieldnameExtractorSpec extends FlatSpec {

  case class A(id: Int, value: Option[String])
  case class B(id: Int, a: A)
  case class X()

  "FieldnameExtractor" should "get the fieldnames of a case class" in {
    fieldNames[A] shouldBe List("id", "value")
    fieldNames[B] shouldBe List("id", "a")
    fieldNames[X] shouldBe Nil
  }

  "FieldnameExtractor" should "get the fieldnames and types of a case class" in {
    fieldNameTypes[A] shouldBe List(("id", "Int"), ("value", "Option"))
    fieldNameTypes[B] shouldBe List(("id", "Int"), ("a", "A"))
    fieldNameTypes[X] shouldBe Nil
  }

}
