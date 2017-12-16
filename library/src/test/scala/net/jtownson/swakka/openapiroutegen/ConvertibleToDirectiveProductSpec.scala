package net.jtownson.swakka.openapiroutegen

import net.jtownson.swakka.coreroutegen.{ConvertibleToDirective}
import net.jtownson.swakka.openapimodel._
import org.scalatest.FlatSpec
import org.scalatest.Matchers._
import shapeless.{::, HNil}

// Investigate the usage of three styles of Params definition for ConvertibleToDirective
// 1) Case classes
// 2) HLists
// 3) Tuples
class ConvertibleToDirectiveProductSpec extends FlatSpec {

  "Zero param formats" should "work for products" in {

    case class EmptyParams()

    implicitly[ConvertibleToDirective.Aux[HNil, HNil]]

    implicitly[ConvertibleToDirective.Aux[Unit, HNil]]

    implicitly[ConvertibleToDirective.Aux[EmptyParams, HNil]]
  }

  "single param formats" should "work" in {

    case class Parameters1(p1: QueryParameter[String])

    implicitly[ConvertibleToDirective.Aux[QueryParameter[String] :: HNil, String :: HNil]]

    implicitly[ConvertibleToDirective.Aux[Tuple1[QueryParameter[String]], String :: HNil]]

    implicitly[ConvertibleToDirective.Aux[Parameters1, String :: HNil]]
  }

  "multiple param formats" should "work" in {

    case class Parameters2(p1: QueryParameter[String], p2: HeaderParameter[Option[Int]])

    implicitly[ConvertibleToDirective.Aux[QueryParameter[String] :: HeaderParameter[Option[Int]] :: HNil, String :: Option[Int] :: HNil]]

    implicitly[ConvertibleToDirective.Aux[(QueryParameter[String], HeaderParameter[Option[Int]]), String :: Option[Int] :: HNil]]

    implicitly[ConvertibleToDirective.Aux[Parameters2, String :: Option[Int] :: HNil]]
  }
}
