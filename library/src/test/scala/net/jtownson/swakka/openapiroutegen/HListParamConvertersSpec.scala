package net.jtownson.swakka.openapiroutegen

import akka.http.scaladsl.server.Directive1
import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.Route.seal
import net.jtownson.swakka.openapiroutegen.OpenApiDirective.hNilConverter
import org.scalatest.FlatSpec
import shapeless.HNil
import org.scalatest.Matchers._
import akka.http.scaladsl.model.StatusCodes._

class HListParamConvertersSpec extends FlatSpec with ConverterTest {

  it should "pass HNil" in {

    val toDirective: Directive1[HNil] = hNilConverter.convertToDirective("/a/b", HNil)

    val route = toDirective { _ =>
      complete("matched")
    }

    Get("http://localhost/a/b") ~> route ~> check {
      status shouldBe OK
      responseAs[String] shouldBe "matched"
    }

    Get("http://localhost/x/y") ~> seal(route) ~> check {
      status shouldBe NotFound
    }
  }
}
