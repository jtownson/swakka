package net.jtownson.swakka.openapiroutegen

import akka.http.scaladsl.server.Route
import net.jtownson.swakka.coreroutegen.RouteGen
import net.jtownson.swakka.openapimodel._
import org.scalatest.FlatSpec
import org.scalatest.Matchers._
import shapeless.{::, HNil}
import spray.json._

// Investigate the usage of three styles of routegen paths definition
// 1) Case classes
// 2) HLists
// 3) Tuples
class RouteGenProductSpec extends FlatSpec {

  "Zero path formats" should "work for products" in {

    case class EmptyPaths()

    implicitly[RouteGen[HNil]]

    implicitly[RouteGen[Unit]]

    implicitly[RouteGen[EmptyPaths]]
  }

  "single path api" should "work" in {

    type Path = PathItem[QueryParameter[String] :: HNil, (String) => Route, HNil]

    case class Paths(p1: Path)

    implicitly[RouteGen[Path :: HNil]]

    implicitly[RouteGen[Tuple1[Path]]]

    implicitly[RouteGen[Paths]]
  }

  "multiple path api" should "work" in {

    type Path1 = PathItem[QueryParameter[String] :: HNil, (String) => Route, HNil]
    type Path2 = PathItem[QueryParameter[Long] :: HNil, (Long) => Route, HNil]

    case class Paths(p1: Path1, p2: Path2)

    implicitly[RouteGen[Path1 :: Path2 :: HNil]]

    implicitly[RouteGen[(Path1, Path2)]]

    implicitly[RouteGen[Paths]]
  }
}
