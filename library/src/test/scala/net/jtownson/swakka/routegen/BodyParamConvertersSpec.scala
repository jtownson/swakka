package net.jtownson.swakka.routegen

import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.Route
import net.jtownson.swakka.OpenApiJsonProtocol._
import net.jtownson.swakka.jsonschema.SchemaWriter.schemaWriter
import net.jtownson.swakka.model.Parameters.BodyParameter
import net.jtownson.swakka.routegen.ConvertibleToDirective.{bodyOptParamConverter, bodyParamConverter}
import org.scalatest.FlatSpec
import spray.json._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

class BodyParamConvertersSpec extends FlatSpec with ConverterTest {

  it should "convert a body param of String" in {
    converterTest[String, BodyParameter[String]](post("/p", "Hello"), "Hello", BodyParameter[String]('p))
    converterTest[Option[String], BodyParameter[Option[String]]](post("/p", "Hello"), "Some(Hello)", BodyParameter[Option[String]]('p))
    converterTest[Option[String], BodyParameter[Option[String]]](post("/p"), "None", BodyParameter[Option[String]]('p))
    converterTest[String, BodyParameter[String]](post("/p"), "Hello", BodyParameter[String]('p, default = Some("Hello")))
    converterTest[Option[String], BodyParameter[Option[String]]](post("/p"), "Some(Hello)", BodyParameter[Option[String]]('p, default = Some(Some("Hello"))))
  }

  it should "convert a required body param of a case class" in {
    implicit val petFormat = jsonFormat2(Pet)
    implicit val petSchemaWriter = schemaWriter(Pet)
    implicit val ev: ConvertibleToDirective[BodyParameter[Pet]] = bodyParamConverter[Pet]

    val pet = Pet(1, "tiddles").toJson.compactPrint

    val route: Route = ev.convertToDirective("", BodyParameter[Pet]('p)) { bp =>
      complete(bp.value)
    }
    converterTest[Pet, BodyParameter[Pet]](post("/p", pet), pet, route)
  }

  it should "convert an optional body param of a case class" in {
    implicit val petFormat = jsonFormat2(Pet)
    implicit val petSchemaWriter = schemaWriter(Pet)
    implicit val ev: ConvertibleToDirective[BodyParameter[Option[Pet]]] = bodyParamConverter[Option[Pet]]

    val route: Route = bodyOptParamConverter[Pet].convertToDirective("", BodyParameter[Option[Pet]]('p)) { bp =>
      bp.value match {
        case Some(pet) => fail("should have got nothing")
        case None => complete("got nothing")
      }
    }

    converterTest[Option[Pet], BodyParameter[Option[Pet]]](post("/p"), "got nothing", route)
  }
}
