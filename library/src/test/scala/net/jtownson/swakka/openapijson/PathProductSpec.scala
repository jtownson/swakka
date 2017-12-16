package net.jtownson.swakka.openapijson

import akka.http.scaladsl.server.Route
import net.jtownson.swakka.openapimodel._
import org.scalatest.FlatSpec
import org.scalatest.Matchers._
import shapeless.{HNil, ::}
import spray.json.JsonFormat

// Investigate the usage of three styles of paths definition
// 1) Case classes
// 2) HLists
// 3) Tuples
class PathProductSpec extends FlatSpec {

  "Zero path formats" should "work for products" in {

    case class ZeroPaths()

    implicitly[JsonFormat[OpenApi[HNil, Nothing]]]

    implicitly[JsonFormat[OpenApi[Unit, Nothing]]]

    implicitly[JsonFormat[OpenApi[ZeroPaths, Nothing]]]
  }

  "Single path formats" should "work for products" in {

    case class EmptyParams()
    type PathType = PathItem[EmptyParams, () => Route, HNil]

    case class OnePath(p1: PathType)

    implicitly[JsonFormat[OpenApi[OnePath, Nothing]]]

    implicitly[JsonFormat[OpenApi[Tuple1[PathType], Nothing]]]

    implicitly[JsonFormat[OpenApi[PathType :: HNil, Nothing]]]
  }
}
