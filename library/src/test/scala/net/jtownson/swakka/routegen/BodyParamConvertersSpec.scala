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
import akka.http.scaladsl.model.StatusCodes.{BadRequest, OK}

class BodyParamConvertersSpec extends FlatSpec with ConverterTest {

  implicit val petFormat = jsonFormat2(Pet)
  implicit val petSchemaWriter = schemaWriter(Pet)

  "BodyParamConverters" should "convert a body param of String" in {
    converterTest[String, BodyParameter[String]](post("/p", "Hello"), "Hello", BodyParameter[String]('p))
    converterTest[Option[String], BodyParameter[Option[String]]](post("/p", "Hello"), "Some(Hello)", BodyParameter[Option[String]]('p))
    converterTest[Option[String], BodyParameter[Option[String]]](post("/p"), "None", BodyParameter[Option[String]]('p))
    converterTest[String, BodyParameter[String]](post("/p"), "Hello", BodyParameter[String]('p, default = Some("Hello")))
    converterTest[Option[String], BodyParameter[Option[String]]](post("/p"), "Some(Hello)", BodyParameter[Option[String]]('p, default = Some(Some("Hello"))))
  }

  they should "convert a required body param of a case class" in {

    val pet = Pet(1, "tiddles").toJson.compactPrint
    val conv = implicitly[ConvertibleToDirective[BodyParameter[Pet]]]

    val route: Route = conv.convertToDirective("", BodyParameter[Pet]('p)) { bp =>
      complete(bp.value)
    }
    converterTest[Pet, BodyParameter[Pet]](post("/p", pet), pet, route)
  }

  they should "convert an optional body param of a case class" in {

    val conv = implicitly[ConvertibleToDirective[BodyParameter[Option[Pet]]]]

    val route: Route = conv.convertToDirective("", BodyParameter[Option[Pet]]('p)) { bp =>
      bp.value match {
        case Some(_) => fail("should have got nothing")
        case None => complete("got nothing")
      }
    }

    converterTest[Option[Pet], BodyParameter[Option[Pet]]](post("/p"), "got nothing", route)
  }

  they should "pass enumerated body parameters iff the request provides a valid enum value" in {
    val pet1 = Pet(1, "pet1")
    val pet2 = Pet(2, "pet2")
    val bp = BodyParameter[Pet]('body, enum = Some(Seq(pet1)))
    converterTest[Pet, BodyParameter[Pet]](Post("/p", pet1), bp, OK)
    converterTest[Pet, BodyParameter[Pet]](Post("/p", pet2), bp, BadRequest)
  }

  they should "pass enumerated optional body parameters iff the request provides a valid enum value" in {
    val pet1 = Pet(1, "pet1")
    val pet2 = Pet(2, "pet2")
    val bp = BodyParameter[Option[Pet]]('body, enum = Some(Seq(Some(pet1))))
    converterTest[Option[Pet], BodyParameter[Option[Pet]]](Post("/p", pet1), bp, OK)
    converterTest[Option[Pet], BodyParameter[Option[Pet]]](Post("/p", pet2), bp, BadRequest)
  }
}
