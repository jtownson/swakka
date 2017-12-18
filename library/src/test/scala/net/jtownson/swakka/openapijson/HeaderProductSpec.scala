package net.jtownson.swakka.openapijson

import net.jtownson.swakka.openapimodel._
import org.scalatest.FlatSpec
import org.scalatest.Matchers._
import shapeless.{::, HNil}

// Investigate the usage of three styles of header definition
// 1) Case classes
// 2) HLists
// 3) Tuples
class HeaderProductSpec extends FlatSpec {

  "Zero header formats" should "work for products" in {

    case class NoHeaders()

    implicitly[HeadersJsonFormat[HNil]]

    implicitly[HeadersJsonFormat[Unit]]

    implicitly[HeadersJsonFormat[NoHeaders]]
  }

  "Single header formats" should "work for products" in {

    case class SingleHeader(h1: Header[String])

    implicitly[HeadersJsonFormat[Header[String] :: HNil]]

    implicitly[HeadersJsonFormat[Tuple1[Header[String]]]]

    implicitly[HeadersJsonFormat[SingleHeader]]
  }

  "Multiple header formats" should "work for products" in {

    case class MultipleHeaders(h1: Header[String], h2: Header[Long])

    implicitly[HeadersJsonFormat[Header[String] :: Header[Long] :: HNil]]

    implicitly[HeadersJsonFormat[(Header[String], Header[Long])]]

    implicitly[HeadersJsonFormat[MultipleHeaders]]
  }
}
