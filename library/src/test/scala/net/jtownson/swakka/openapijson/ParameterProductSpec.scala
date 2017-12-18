package net.jtownson.swakka.openapijson

import akka.http.scaladsl.server.Route
import net.jtownson.swakka.openapimodel.{Parameter, _}
import org.scalatest.FlatSpec
import org.scalatest.Matchers._
import spray.json._
import shapeless.{::, HNil}

// Investigate the usage of three styles of parameter definition
// 1) Case classes
// 2) HLists
// 3) Tuples
class ParameterProductSpec extends FlatSpec {

  "Zero param formats" should "work for products" in {

    case class EmptyParameters()

    implicitly[JsonFormat[OpenApi[PathItem[EmptyParameters, () => Route, HNil] :: HNil, Nothing]]]

    implicitly[JsonFormat[OpenApi[PathItem[HNil, () => Route, HNil] :: HNil, Nothing]]]

  }

  "single param api" should "work" in {

    case class Parameters1(p1: QueryParameter[String])

    implicitly[JsonFormat[OpenApi[PathItem[Parameters1, String => Route, HNil] :: HNil, Nothing]]]

    implicitly[JsonFormat[OpenApi[PathItem[QueryParameter[String] :: HNil, String => Route, HNil] :: HNil, Nothing]]]

    implicitly[JsonFormat[OpenApi[PathItem[Tuple1[QueryParameter[String]], (String) => Route, HNil] :: HNil, Nothing]]]
  }

  "two param api" should "work" in {

    case class Parameters2(p1: QueryParameter[String], p2: HeaderParameter[Option[Int]])

    implicitly[JsonFormat[OpenApi[PathItem[Parameters2, (String, Option[Int]) => Route, HNil] :: HNil, Nothing]]]

    implicitly[JsonFormat[OpenApi[PathItem[QueryParameter[String] :: HeaderParameter[Option[Int]] :: HNil, (String, Option[Int]) => Route, HNil] :: HNil, Nothing]]]

    implicitly[JsonFormat[OpenApi[PathItem[(QueryParameter[String], HeaderParameter[Option[Int]]), (String, Option[Int]) => Route, HNil] :: HNil, Nothing]]]
  }
}
