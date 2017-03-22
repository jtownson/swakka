package net.jtownson.openapi

import akka.http.scaladsl.testkit.ScalatestRouteTest
import net.jtownson.openapi.OpenApiRouter._
import org.scalatest.FlatSpec
import org.scalatest.Matchers._

import scala.io.Source

class OpenApiRouterSpec extends FlatSpec with ScalatestRouteTest {

  "parseOpenApiSource" should "parse minimal ruok definition" in {
//    parseOpenApiSource(
//      resource("/net/jtownson/openapi/ruok.minimal.json")) shouldBe TestDefinitions.ruokMinimal
  }

  "akkaRoute" should "be correct for minimal ruok defintion" in {

  }

  private def resource(resource: String): Source =
    Source.fromInputStream(getClass.getResourceAsStream(resource))

}
