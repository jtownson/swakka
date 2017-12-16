package net.jtownson.swakka.openapijson

import net.jtownson.swakka.openapimodel._
import org.scalatest.FlatSpec
import org.scalatest.Matchers._
import shapeless.record.Record
import shapeless.{::, HNil}
import shapeless.syntax.singleton._

// Investigate the usage of three styles of security definition
// 1) Case classes
// 2) HLists
// 3) Tuples
class SecurityDefinitionsProductSpec extends FlatSpec {

  "Zero security formats" should "work for products" in {

    case class NoSecurity()

    implicitly[SecurityDefinitionsJsonFormat[HNil]]

    implicitly[SecurityDefinitionsJsonFormat[Unit]]

    implicitly[SecurityDefinitionsJsonFormat[NoSecurity]]
  }

  "Single security formats" should "work for products" in {

    case class SingleSecurityDef(d1: ApiKeyInHeaderSecurity)

    type SecurityDef = Record.`'d1 -> ApiKeyInHeaderSecurity`.T

    implicitly[SecurityDefinitionsJsonFormat[SecurityDef]]

    implicitly[SecurityDefinitionsJsonFormat[SingleSecurityDef]]
  }

  "Multiple security formats" should "work for products" in {

    case class MultipleSecurityDef(d1: ApiKeyInHeaderSecurity, d2: Oauth2AccessCodeSecurity)

    type SecurityDef = Record.`'d1 -> ApiKeyInHeaderSecurity, 'd2 -> Oauth2AccessCodeSecurity`.T

    implicitly[SecurityDefinitionsJsonFormat[SecurityDef]]

    implicitly[SecurityDefinitionsJsonFormat[MultipleSecurityDef]]
  }
}
